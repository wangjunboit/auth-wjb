package com.wjb.auth.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.wjb.auth.gateway.security.ApiPermCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/** 网关统一鉴权:登录态 + 动态 RBAC(查映射决定所需权限码),失败返回统一 JSON */
@Configuration
public class SaTokenGatewayConfig {

    /** 鉴权白名单(放行:无需登录) */
    private static final String[] WHITELIST = {
            "/auth/login", "/auth/login/**", "/auth/sms-code", "/auth/email-code",
            "/auth/oauth/**", "/auth/captcha", "/health", "/doc.html",
            "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/favicon.ico"
    };

    @Bean
    public SaReactorFilter saReactorFilter(ApiPermCache apiPermCache) {
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                    // 1) 白名单外:必须登录
                    SaRouter.match("/**").notMatch(WHITELIST)
                            .check(r -> StpUtil.checkLogin());

                    // 2) 动态权限校验:查映射,命中才校验权限码,未命中=只需登录
                    SaRouter.match("/**").notMatch(WHITELIST)
                            .check(r -> {
                                String method = SaHolder.getRequest().getMethod();
                                String path = SaHolder.getRequest().getRequestPath();
                                String perm = apiPermCache.requiredPerm(method, path);
                                if (perm != null) {
                                    StpUtil.checkPermission(perm);
                                }
                            });
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
