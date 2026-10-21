package com.jexis.jexis_backend.common.ratelimit;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RateLimitService {
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    private final RateLimitProperties properties;
    private final ProxyManager<String> manager;

    public RateLimitService(RateLimitProperties properties, ObjectProvider<ProxyManager<String>> manager) {
        this.properties = properties;
        this.manager = manager.getIfAvailable();
    }

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public void check(String policy, String identity) {
        long retryAfter = consume(policy, identity);
        if (retryAfter > 0) throw new RateLimitException(retryAfter);
    }

    // Silently suppress repeated reset emails without revealing whether an account exists.
    public boolean allowResetEmail(String email) {
        return consume("reset-email", normalizeEmail(email)) == 0;
    }

    private long consume(String policy, String identity) {
        if (!properties.isEnabled()) return 0;
        var limit = properties.getPolicies().get(policy);
        if (limit == null) throw new IllegalArgumentException("Unknown rate limit policy: " + policy);
        var config = BucketConfiguration.builder()
                .addLimit(b -> b.capacity(limit.capacity()).refillGreedy(limit.capacity(), limit.period()))
                .build();
        // Include policy configuration so a changed limit takes effect immediately.
        String key = "jexis:rate-limit:v1:" + policy + ":" + limit.capacity() + ":"
                + limit.period().toMillis() + ":" + HashUtils.sha256(identity);
         try {
            var probe = manager.builder().build(key, () -> config).tryConsumeAndReturnRemaining(1);
            return probe.isConsumed() ? 0 : Math.max(1,
                    TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill() - 1) + 1);
        } catch (RuntimeException ex) {
             System.out.println("Rate limit storage unavailable for policy " + policy + ": " + ex.getMessage());
            // Do not log bucket keys, email addresses, tokens, or Redis connection credentials.
            log.warn("Rate limit storage unavailable for policy {}", policy);
            throw new RateLimitException(ex);
        }
    }
}
