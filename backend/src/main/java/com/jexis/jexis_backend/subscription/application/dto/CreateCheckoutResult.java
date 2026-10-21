package com.jexis.jexis_backend.subscription.application.dto;

public record CreateCheckoutResult(String checkoutUrl, boolean directSubscriptionChange) {
    public static CreateCheckoutResult checkoutCreated(String checkoutUrl) {
        return new CreateCheckoutResult(checkoutUrl, false);
    }

    public static CreateCheckoutResult changedSubscription() {
        return new CreateCheckoutResult(null, true);
    }
}
