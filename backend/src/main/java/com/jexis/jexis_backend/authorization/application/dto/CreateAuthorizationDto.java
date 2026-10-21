package com.jexis.jexis_backend.authorization.application.dto;

import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;

public record CreateAuthorizationDto(
        String walletId,
        String stripeAuthorizationId,
        String cardId,
        Boolean approved,
        Long amount,
        String currency,
        AuthorizationStatus status,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry
) {
}
