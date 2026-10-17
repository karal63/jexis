package com.jexis.jexis_backend.wallet.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;

public record WalletResponseDto(
        UUID id,
        String name,
        AccountResponseDto account,
        Long availableBalance,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
