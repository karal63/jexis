package com.jexis.jexis_backend.externalAccount.application.dto;

import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExternalAccountResponseDto(
        UUID id,
        AccountResponseDto accountId,
        String bankName,
        String last4,
        String country,
        String currency,
        boolean isDefault,
        LocalDateTime createdAt
) {
}
