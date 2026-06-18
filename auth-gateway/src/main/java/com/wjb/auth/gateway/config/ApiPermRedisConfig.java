package com.wjb.auth.gateway.config;

import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.gateway.security.ApiPermCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** 订阅 Redis 刷新频道,收到消息时由 ApiPermCache 重载映射 */
@Configuration
public class ApiPermRedisConfig {

    @Bean
    public RedisMessageListenerContainer apiPermListenerContainer(
            RedisConnectionFactory connectionFactory, ApiPermCache apiPermCache) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(apiPermCache,
                new ChannelTopic(SecurityConstants.API_PERM_REFRESH_CHANNEL));
        return container;
    }
}
