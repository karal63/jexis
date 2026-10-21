package com.jexis.jexis_backend.dispute.domain.enums;

import lombok.Getter;

@Getter
public enum DisputeStatus {
    NEEDS_RESPONSE(10),

    WARNING_NEEDS_RESPONSE(20),

    UNDER_REVIEW(30),

    WON(40),

    LOST(40);

    private final int priority;

    DisputeStatus(int priority) {
        this.priority = priority;
    }

    public boolean isTerminal() {
        return this == WON || this == LOST;
    }

    public static DisputeStatus fromStripeStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return NEEDS_RESPONSE;
        }

        return switch (stripeStatus.toLowerCase()) {
            case "needs_response", "unsubmitted" -> NEEDS_RESPONSE;
            case "warning_needs_response" -> WARNING_NEEDS_RESPONSE;
            case "submitted", "under_review" -> UNDER_REVIEW;
            case "won" -> WON;
            case "lost" -> LOST;
            default -> NEEDS_RESPONSE;
        };
    }
}
