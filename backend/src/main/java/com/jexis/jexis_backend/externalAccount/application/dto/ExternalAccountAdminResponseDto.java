package com.jexis.jexis_backend.externalAccount.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountAdminResponseDto;

public record ExternalAccountAdminResponseDto(
        UUID id,
        AccountAdminResponseDto account,
        String stripeExternalAccountId,
        String bankName,
        String last4,
        String country,
        String currency,
        boolean isDefault,
        Boolean isDeleted,
        LocalDateTime deletedAt,
        LocalDateTime createdAt) {

}
