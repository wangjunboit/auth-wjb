package com.wjb.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("auth-service API")
                .description("认证业务服务接口文档")
                .version("v0.0.1"));
    }
}
