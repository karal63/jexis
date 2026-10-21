package com.jexis.jexis_backend.dispute.application.dto;

import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import com.jexis.jexis_backend.transaction.application.dto.TransactionResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisputeResponseDto(
        UUID id,
        TransactionResponseDto transaction,
        WalletResponseDto wallet,
        Long amount,
        String currency,
        DisputeStatus status,
        DisputeReason reason,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
