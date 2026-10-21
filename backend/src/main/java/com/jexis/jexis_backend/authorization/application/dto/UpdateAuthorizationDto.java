package com.jexis.jexis_backend.authorization.application.dto;

import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;

public record UpdateAuthorizationDto(
        String stripeAuthorizationId,
        Boolean approved,
        Long amount,
        AuthorizationStatus status,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry
) {
}
