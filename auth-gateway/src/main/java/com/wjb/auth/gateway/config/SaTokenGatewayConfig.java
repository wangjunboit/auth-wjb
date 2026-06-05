package com.wjb.auth.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/** 网关统一鉴权:登录态 + 按路由/方法校验权限码,失败返回统一 JSON */
@Configuration
public class SaTokenGatewayConfig {

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                    // 1) 白名单放行
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

                    // 2) 用户管理:按路由 + HTTP 方法校验权限码(先匹配路径,再链式匹配方法)
                    SaRouter.match("/system/user/list").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:user:list"));
                    SaRouter.match("/system/user").match(SaHttpMethod.POST)
                            .check(r -> StpUtil.checkPermission("system:user:add"));
                    SaRouter.match("/system/user").match(SaHttpMethod.PUT)
                            .check(r -> StpUtil.checkPermission("system:user:edit"));
                    SaRouter.match("/system/user/**").match(SaHttpMethod.DELETE)
                            .check(r -> StpUtil.checkPermission("system:user:remove"));
                    // 详情 GET /system/user/{id}(排除 /list)
                    SaRouter.match("/system/user/*").notMatch("/system/user/list").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:user:query"));
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
