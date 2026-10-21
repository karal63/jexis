package com.jexis.jexis_backend.common.ratelimit;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("application.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private String redisUri = "redis://localhost:6379";
    private Duration timeout = Duration.ofSeconds(1);
    private Map<String, Limit> policies = new HashMap<>(Map.of(
            "login-ip", new Limit(30, Duration.ofMinutes(1)),
            "login-identity", new Limit(5, Duration.ofMinutes(1)),
            "signup-ip", new Limit(5, Duration.ofHours(1)),
            "reset-ip", new Limit(10, Duration.ofHours(1)),
            "reset-email", new Limit(3, Duration.ofHours(1)),
            "confirm-ip", new Limit(10, Duration.ofMinutes(1)),
            "activate-ip", new Limit(10, Duration.ofMinutes(1)),
            "refresh-ip", new Limit(60, Duration.ofMinutes(1)),
            "refresh-user", new Limit(30, Duration.ofMinutes(1))));

    public record Limit(long capacity, Duration period) {
        public Limit {
            if (capacity <= 0 || period == null || period.isZero() || period.isNegative()) {
                throw new IllegalArgumentException("Rate limit capacity and period must be positive");
            }
        }
    }
}
