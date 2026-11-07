package com.jexis.jexis_backend.account.domain.enums;

public enum OnboardingStatus {
    NOT_STARTED,
    ACTION_REQUIRED,
    PENDING_VERIFICATION,
    COMPLETED,
    RESTRICTED;

    public boolean requiresOnboarding() {
        return this == NOT_STARTED || this == ACTION_REQUIRED;
    }
}
