package com.wjb.auth.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import com.wjb.auth.gateway.security.ApiPermCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关统一鉴权:只拦 /api/**,判定时剥掉 /api 前缀得到逻辑路径(/auth/.. /system/..),
 * 复用白名单 + 动态RBAC 映射。静态资源(非 /api)不被拦截,由 WebFlux 静态处理器/SpaWebFilter 处理。
 */
@Configuration
public class SaTokenGatewayConfig {

    /** 鉴权白名单(逻辑路径,无需登录) */
    private static final String[] WHITELIST = {
            "/auth/login", "/auth/login/**", "/auth/sms-code", "/auth/email-code",
            "/auth/oauth/**", "/auth/captcha", "/health", "/doc.html",
            "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/favicon.ico"
    };

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Bean
    public SaReactorFilter saReactorFilter(ApiPermCache apiPermCache) {
        return new SaReactorFilter()
                .addInclude("/api/**")
                .setAuth(obj -> {
                    String path = SaHolder.getRequest().getRequestPath();
                    // 剥掉 /api 前缀得到逻辑路径
                    String logical = path.startsWith("/api") ? path.substring(4) : path;
                    if (logical.isEmpty()) {
                        logical = "/";
                    }
                    // 白名单放行
                    for (String w : WHITELIST) {
                        if (PATH_MATCHER.match(w, logical)) {
                            return;
                        }
                    }
                    // 登录态
                    StpUtil.checkLogin();
                    // 动态权限:命中映射才校验权限码
                    String method = SaHolder.getRequest().getMethod();
                    String perm = apiPermCache.requiredPerm(method, logical);
                    if (perm != null) {
                        StpUtil.checkPermission(perm);
                    }
                })
                .setError(e -> {
                    SaHolder.getResponse().setHeader("Content-Type", "application/json; charset=utf-8");
                    Map<String, Object> body = new HashMap<>();
                    if (e instanceof NotLoginException) {
                        body.put("code", 401);
                        body.put("msg", "登录已过期,请重新登录");
                    } else if (e instanceof NotPermissionException) {
                        body.put("code", 403);
                        body.put("msg", "无操作权限");
                    } else {
                        body.put("code", 500);
                        body.put("msg", e.getMessage());
                    }
                    body.put("data", null);
                    return body;
                });
    }
}
