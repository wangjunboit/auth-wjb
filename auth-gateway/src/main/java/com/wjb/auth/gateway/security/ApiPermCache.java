package com.wjb.auth.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.common.rbac.ApiPermDef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** 网关侧映射缓存:启动从 Redis 加载,订阅刷新频道实时重载;对外提供按请求查权限码。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiPermCache implements ApplicationRunner, MessageListener {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private volatile ApiPermMatcher matcher = new ApiPermMatcher(List.of());

    @Override
    public void run(ApplicationArguments args) {
        load();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        load();
    }

    /** 从 Redis 读 JSON 重建快照;失败则沿用旧快照 */
    public void load() {
        try {
            String json = redis.opsForValue().get(SecurityConstants.API_PERM_KEY);
            List<ApiPermDef> defs = (json == null || json.isBlank())
                    ? List.of()
                    : objectMapper.readerForListOf(ApiPermDef.class).readValue(json);
            this.matcher = new ApiPermMatcher(defs);
            log.info("已加载 API 权限映射 {} 条", defs.size());
        } catch (Exception e) {
            log.warn("加载 API 权限映射失败,沿用旧快照", e);
        }
    }

    /** 返回该请求所需权限码;无映射返回 null(=只需登录) */
    public String requiredPerm(String method, String path) {
        return matcher.requiredPerm(method, path);
    }
}
