package com.wjb.auth.service;

import com.wjb.auth.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/** 短信/邮箱验证码:生成、存储、限频、校验(全部走 Redis) */
@Service
@RequiredArgsConstructor
public class VerifyCodeService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration LIMIT_TTL = Duration.ofSeconds(60);
    private final SecureRandom random = new SecureRandom();

    private final StringRedisTemplate redis;

    /** 生成并存码;codeKey 形如 sms:{phone} 或 email:{email};limitKey 形如 sms:limit:{phone} */
    public String generateAndStore(String codeKey, String limitKey) {
        if (Boolean.TRUE.equals(redis.hasKey(limitKey))) {
            throw new ServiceException("发送过于频繁,请稍后再试");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        redis.opsForValue().set(codeKey, code, CODE_TTL);
        redis.opsForValue().set(limitKey, "1", LIMIT_TTL);
        return code;
    }

    /** 校验码:比对后删除(一次性);失败抛异常 */
    public void verifyAndConsume(String codeKey, String inputCode) {
        String code = redis.opsForValue().get(codeKey);
        if (code == null) {
            throw new ServiceException("验证码已过期,请重新获取");
        }
        if (!code.equals(inputCode == null ? null : inputCode.trim())) {
            throw new ServiceException("验证码错误");
        }
        redis.delete(codeKey);
    }
}
