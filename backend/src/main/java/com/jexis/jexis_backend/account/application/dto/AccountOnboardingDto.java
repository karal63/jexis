package com.jexis.jexis_backend.account.application.dto;

import com.jexis.jexis_backend.account.domain.enums.OnboardingStatus;

public record AccountOnboardingDto(OnboardingStatus status, boolean canOnboard, String url) {}
