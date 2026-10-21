package com.jexis.jexis_backend.subscription.application.dto;

import java.util.UUID;

public record EffectiveSubscriptionEntitlementDto(
        UUID entitlementId,
        String key,
        String type,
        String description,
        String value,
        Source source
) {
    public enum Source {
        PLAN,
        SUBSCRIPTION
    }
}
