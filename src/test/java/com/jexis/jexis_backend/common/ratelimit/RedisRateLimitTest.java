package com.jexis.jexis_backend.common.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.ArrayList;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@Tag("redis")
class RedisRateLimitTest {
    private RedisClient client;
    private StatefulRedisConnection<String, byte[]> firstConnection;
    private StatefulRedisConnection<String, byte[]> secondConnection;
    private RateLimitService first;
    private RateLimitService second;
    private RateLimitProperties properties;
    private final String identity = UUID.randomUUID().toString();

    @BeforeEach void setup() {
        properties = new RateLimitProperties();
        properties.setRedisUri(System.getenv("TEST_REDIS_URI"));
        properties.getPolicies().put("login-ip", new RateLimitProperties.Limit(5, Duration.ofHours(1)));
        var config = new RateLimitConfiguration();
        client = config.rateLimitRedisClient(properties);
        firstConnection = config.rateLimitRedisConnection(client);
        secondConnection = config.rateLimitRedisConnection(client);
        first = service(config.rateLimitProxyManager(firstConnection, properties));
        second = service(config.rateLimitProxyManager(secondConnection, properties));
    }

    private RateLimitService service(ProxyManager<String> manager) {
        @SuppressWarnings("unchecked")
        ObjectProvider<ProxyManager<String>> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(manager);
        return new RateLimitService(properties, provider);
    }

    @AfterEach void close() {
        if (firstConnection != null) firstConnection.close();
        if (secondConnection != null) secondConnection.close();
        if (client != null) client.shutdown();
    }

    @Test void springWiresRedisManagerAndBindsPolicyOverrides() {
        try (var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new org.springframework.core.env.MapPropertySource("redis-test", java.util.Map.of(
                            "application.rate-limit.redis-uri", System.getenv("TEST_REDIS_URI"),
                            "application.rate-limit.policies.login-ip.capacity", "1",
                            "application.rate-limit.policies.login-ip.period", "1h")));
            context.register(RateLimitConfiguration.class, RateLimitService.class);
            context.refresh();
            var service = context.getBean(RateLimitService.class);
            service.check("login-ip", identity);
            assertThatThrownBy(() -> service.check("login-ip", identity))
                    .isInstanceOfSatisfying(RateLimitException.class, ex -> assertThat(ex.status()).isEqualTo(429));
            assertThat(service.allowResetEmail(identity + "@example.com")).isTrue();
        }
    }

    @Test void concurrentClientsShareOneAllowance() throws Exception {
        var executor = Executors.newFixedThreadPool(8);
        try {
            var calls = new ArrayList<Callable<Boolean>>();
            for (int i = 0; i < 40; i++) {
                var limiter = i % 2 == 0 ? first : second;
                calls.add(() -> {
                    try { limiter.check("login-ip", identity); return true; }
                    catch (RateLimitException ex) {
                        assertThat(ex.status()).isEqualTo(429);
                        assertThat(ex.retryAfter()).isPositive();
                        return false;
                    }
                });
            }
            long accepted = 0;
            for (var result : executor.invokeAll(calls)) if (result.get()) accepted++;
            assertThat(accepted).isEqualTo(5);
            assertThatCode(() -> first.check("login-ip", identity + "other")).doesNotThrowAnyException();
        } finally { executor.shutdownNow(); }
    }

    @Test void resetRecipientAllowanceIsSharedAndCaseNormalized() {
        String email = identity + "@example.com";
        assertThat(first.allowResetEmail(email)).isTrue();
        assertThat(second.allowResetEmail(email.toUpperCase())).isTrue();
        assertThat(first.allowResetEmail(" " + email + " ")).isTrue();
        assertThat(second.allowResetEmail(email)).isFalse();
    }

    @Test void bucketsExpireAndRefill() throws Exception {
        properties.getPolicies().put("login-ip", new RateLimitProperties.Limit(1, Duration.ofMillis(300)));
        first.check("login-ip", identity);
        assertThatThrownBy(() -> second.check("login-ip", identity)).isInstanceOf(RateLimitException.class);
        String key = "jexis:rate-limit:v1:login-ip:1:300:"
                + com.jexis.jexis_backend.common.hashUtils.HashUtils.sha256(identity);
        assertThat(firstConnection.sync().pttl(key)).isBetween(1L, 60_300L);
        Thread.sleep(350);
        assertThatCode(() -> second.check("login-ip", identity)).doesNotThrowAnyException();
    }

    @Test void unavailableRedisDoesNotAllowProtectedOperation() {
        firstConnection.close();
        assertThatThrownBy(() -> first.check("login-ip", identity))
                .isInstanceOfSatisfying(RateLimitException.class, ex -> {
                    assertThat(ex.status()).isEqualTo(503);
                    assertThat(ex.retryAfter()).isEqualTo(5);
                });
    }
}
