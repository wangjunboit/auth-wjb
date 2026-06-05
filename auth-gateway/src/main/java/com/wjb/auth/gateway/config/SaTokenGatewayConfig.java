package com.wjb.auth.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/** 网关统一鉴权:校验登录态 + 按路由校验权限码,失败返回统一 JSON */
@Configuration
public class SaTokenGatewayConfig {

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                // 拦截全部
                .addInclude("/**")
                // 鉴权逻辑
                .setAuth(obj -> {
                    // 1) 白名单放行(登录、发码、OAuth 回调、文档、健康检查)
                    SaRouter.match("/**")
                            .notMatch(
                                    "/auth/login",
                                    "/auth/login/**",
                                    "/auth/sms-code",
                                    "/auth/email-code",
                                    "/auth/oauth/**",
                                    "/health",
                                    "/doc.html",
                                    "/webjars/**",
                                    "/v3/api-docs/**",
                                    "/swagger-ui/**",
                                    "/favicon.ico"
                            )
                            .check(r -> StpUtil.checkLogin());

                    // 2) 路由 → 权限码(用户管理,M2 业务上线后生效;M1 无 token 时已被上面的 checkLogin 拦下)
                    SaRouter.match("/system/user/**", r -> StpUtil.checkPermission("system:user:list"));
                })
                // 鉴权异常 → 统一 JSON
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
                    return body; // 直接作为响应体序列化为 {code,msg,data}
                });
    }
}
