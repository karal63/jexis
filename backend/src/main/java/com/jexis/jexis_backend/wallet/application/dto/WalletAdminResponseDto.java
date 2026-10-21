package com.jexis.jexis_backend.wallet.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountAdminResponseDto;

public record WalletAdminResponseDto(
        UUID id,
        String name,
        String stripeFinancialAccountId,
        AccountAdminResponseDto account,
        Long availableBalance,
        String currency,
        Boolean isDeleted,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
