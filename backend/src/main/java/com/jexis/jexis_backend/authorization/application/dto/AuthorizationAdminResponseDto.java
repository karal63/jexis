package com.jexis.jexis_backend.authorization.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.card.application.dto.CardAdminResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletAdminResponseDto;

public record AuthorizationAdminResponseDto(
        UUID id,
        WalletAdminResponseDto wallet,
        String stripeAuthorizationId,
        CardAdminResponseDto card,
        Boolean approved,
        Long amount,
        String currency,
        String status,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry,
        LocalDateTime createdAt) {

}
