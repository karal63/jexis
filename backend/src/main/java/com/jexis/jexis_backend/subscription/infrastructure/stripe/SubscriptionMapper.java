package com.jexis.jexis_backend.subscription.infrastructure.stripe;

import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {
    public SubscriptionStatus mapSubscriptionStatus(String status) {
        return switch (status) {
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            case "incomplete_expired" -> SubscriptionStatus.INCOMPLETE_EXPIRED;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "active" -> SubscriptionStatus.ACTIVE;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "unpaid" -> SubscriptionStatus.UNPAID;
            case "paused" -> SubscriptionStatus.PAUSED;
            default ->
                    throw new IllegalArgumentException("Unknown subscription status: " + status);
        };
    }
}
