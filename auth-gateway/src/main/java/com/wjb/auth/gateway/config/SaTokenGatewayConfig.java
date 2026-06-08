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

                    // 用户分配角色
                    SaRouter.match("/system/user/assign-roles").match(SaHttpMethod.POST)
                            .check(r -> StpUtil.checkPermission("system:user:edit"));
                    SaRouter.match("/system/user/*/roles").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:user:query"));

                    // 角色管理
                    SaRouter.match("/system/role/list").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:role:list"));
                    SaRouter.match("/system/role/assign-menus").match(SaHttpMethod.POST)
                            .check(r -> StpUtil.checkPermission("system:role:assign"));
                    SaRouter.match("/system/role").match(SaHttpMethod.POST)
                            .check(r -> StpUtil.checkPermission("system:role:add"));
                    SaRouter.match("/system/role").match(SaHttpMethod.PUT)
                            .check(r -> StpUtil.checkPermission("system:role:edit"));
                    SaRouter.match("/system/role/**").match(SaHttpMethod.DELETE)
                            .check(r -> StpUtil.checkPermission("system:role:remove"));
                    SaRouter.match("/system/role/*").notMatch("/system/role/list").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:role:query"));
                    SaRouter.match("/system/role/*/menus").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:role:query"));

                    // 菜单管理
                    SaRouter.match("/system/menu/list").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:menu:list"));
                    SaRouter.match("/system/menu").match(SaHttpMethod.POST)
                            .check(r -> StpUtil.checkPermission("system:menu:add"));
                    SaRouter.match("/system/menu").match(SaHttpMethod.PUT)
                            .check(r -> StpUtil.checkPermission("system:menu:edit"));
                    SaRouter.match("/system/menu/**").match(SaHttpMethod.DELETE)
                            .check(r -> StpUtil.checkPermission("system:menu:remove"));
                    SaRouter.match("/system/menu/*").notMatch("/system/menu/list").match(SaHttpMethod.GET)
                            .check(r -> StpUtil.checkPermission("system:menu:query"));
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
