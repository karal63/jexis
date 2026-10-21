package com.jexis.jexis_backend.transaction.application.dto;

import com.jexis.jexis_backend.card.application.dto.CardResponseDto;
import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.wallet.application.dto.WalletResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TransactionResponseDto
 * Data Transfer Object for transaction responses in the API layer.
 * This DTO is used to serialize transaction entities into JSON responses
 * without exposing internal domain logic or sensitive information.
 * Author: Copilot
 */
public record TransactionResponseDto(
        UUID id,
        WalletResponseDto walletId,
        TransactionType type,
        Long amount,
        String currency,
        Long merchantAmount,
        String merchantCurrency,
        TransactionStatus status,
        TransactionDirection direction,
        String bankName,
        String bankAccountLast4,
        String routingNumber,
        PaymentMethod paymentMethod,
        CardResponseDto cardId,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry,
        LocalDateTime createdAt
) {
}
