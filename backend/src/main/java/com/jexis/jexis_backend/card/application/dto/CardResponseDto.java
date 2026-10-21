package com.jexis.jexis_backend.card.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.cardholder.application.dto.CardHolderResponseDto;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletResponseDto;

public record CardResponseDto(
        UUID id,
        CardHolderResponseDto cardHolder,
        WalletResponseDto treasuryAccount,
        UserResponseDto user,
        String last4,
        CardStatus status,
        List<SpendingLimit> spendingLimits,
        String brand,
        String type,
        String currency,
        Long expYear,
        LocalDateTime createdAt) {
}
