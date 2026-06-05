package com.wjb.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("auth-wjb 用户权限登录系统 API")
                        .description("基于 Sa-Token 的 RBAC 权限系统接口文档")
                        .version("v0.0.1"))
                // 在文档页提供 Authorization header 输入框
                .components(new Components().addSecuritySchemes("Authorization",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}
