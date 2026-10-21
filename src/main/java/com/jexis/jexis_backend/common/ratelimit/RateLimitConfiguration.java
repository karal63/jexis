package com.jexis.jexis_backend.common.ratelimit;

import java.time.Duration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfiguration {
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "application.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    RedisClient rateLimitRedisClient(RateLimitProperties properties) {
        var uri = RedisURI.create(properties.getRedisUri());
        uri.setTimeout(properties.getTimeout());
        var client = RedisClient.create(uri);
        client.setOptions(ClientOptions.builder()
                .socketOptions(SocketOptions.builder().connectTimeout(properties.getTimeout()).build())
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS).build());
        return client;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "application.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    StatefulRedisConnection<String, byte[]> rateLimitRedisConnection(RedisClient rateLimitRedisClient) {
        return rateLimitRedisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    @ConditionalOnProperty(name = "application.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    ProxyManager<String> rateLimitProxyManager(StatefulRedisConnection<String, byte[]> connection, RateLimitProperties properties) {
        return Bucket4jLettuce.casBasedBuilder(connection)
                .requestTimeout(properties.getTimeout())
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                .build();
    }
}
