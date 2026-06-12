package com.wjb.auth.service;

import com.wjb.auth.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/** OAuth state:防 CSRF + 携带模式;value 形如 "login" 或 "bind:{userId}" */
@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private static final String PREFIX = "oauth:state:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public String createLogin() {
        String state = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(PREFIX + state, "login", TTL);
        return state;
    }

    public String createBind(Long userId) {
        String state = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(PREFIX + state, "bind:" + userId, TTL);
        return state;
    }

    /** 解析:返回 [mode, userIdOrNull];校验后删除(一次性) */
    public StateData consume(String state) {
        String key = PREFIX + state;
        String val = redis.opsForValue().get(key);
        if (val == null) {
            throw new ServiceException("授权已过期或非法请求,请重试");
        }
        redis.delete(key);
        if (val.equals("login")) {
            return new StateData("login", null);
        }
        if (val.startsWith("bind:")) {
            return new StateData("bind", Long.valueOf(val.substring(5)));
        }
        throw new ServiceException("非法 state");
    }

    public record StateData(String mode, Long userId) {}
}
