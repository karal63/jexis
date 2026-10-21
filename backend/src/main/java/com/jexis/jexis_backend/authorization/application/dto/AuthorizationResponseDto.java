package com.jexis.jexis_backend.authorization.application.dto;

import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.card.application.dto.CardResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthorizationResponseDto(
        UUID id,
        WalletResponseDto walletId,
        CardResponseDto cardId,
        Boolean approved,
        Long amount,
        String currency,
        AuthorizationStatus status,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry,
        LocalDateTime createdAt
) {
}
