package com.jexis.jexis_backend.subscription.domain.enums;

public enum SubscriptionStatus {
    INCOMPLETE,
    INCOMPLETE_EXPIRED,
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    UNPAID,
    PAUSED
}
