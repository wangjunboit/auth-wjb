package com.wjb.auth.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.wjb.auth.common.constant.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 鉴权通过的请求,用 token 解析出 userId 注入 X-User-Id 头转发下游。
 * 用 StpUtil.getLoginIdByToken 直接查 Redis,无需 SaReactor 上下文。
 */
@Component
public class UserIdRelayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token != null && !token.isBlank()) {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                ServerHttpRequest mutated = exchange.getRequest().mutate()
                        .header(SecurityConstants.HEADER_USER_ID, loginId.toString())
                        .build();
                return chain.filter(exchange.mutate().request(mutated).build());
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 在 Sa-Token 鉴权过滤器之后执行(其默认 order 较小);此处取较大值确保鉴权先行
        return 0;
    }
}
