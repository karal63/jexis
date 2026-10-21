package com.jexis.jexis_backend.dispute.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.transaction.application.dto.TransactionAdminResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletAdminResponseDto;

public record DisputeAdminResponseDto(
        UUID id,
        TransactionAdminResponseDto transaction,
        WalletAdminResponseDto wallet,
        String stripeDisputeId,
        Long amount,
        String currency,
        String status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt) {

}
