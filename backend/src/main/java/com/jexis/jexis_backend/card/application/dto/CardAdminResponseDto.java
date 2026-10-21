package com.jexis.jexis_backend.card.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.cardholder.application.dto.CardHolderAdminResponseDto;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import com.jexis.jexis_backend.user.application.dto.UserAdminResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletAdminResponseDto;

public record CardAdminResponseDto(
        UUID id,
        String stripeCardId,
        CardHolderAdminResponseDto cardHolder,
        WalletAdminResponseDto treasuryAccount,
        UserAdminResponseDto user,
        String last4,
        String status,
        List<SpendingLimit> spendingLimits,
        String brand,
        String type,
        String currency,
        Long expYear,
        Boolean isDeleted,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
