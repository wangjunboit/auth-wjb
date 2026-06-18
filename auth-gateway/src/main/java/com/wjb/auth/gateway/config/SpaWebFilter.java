package com.wjb.auth.gateway.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * SPA history 路由回退:非 /api、非根、且无扩展名(即不是静态资源文件)的路径,
 * 重写为 /index.html,交给 WebFlux 静态资源处理器返回,使前端 history 路由刷新不 404。
 * 静态资源(含扩展名,如 .js/.css/.png)与 /api 请求不受影响。
 */
@Component
public class SpaWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        boolean isApi = path.startsWith("/api");
        boolean isRoot = path.equals("/");
        boolean looksLikeFile = path.contains(".");
        if (!isApi && !isRoot && !looksLikeFile) {
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate().path("/index.html").build())
                    .build());
        }
        return chain.filter(exchange);
    }
}
