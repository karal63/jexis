package com.jexis.jexis_backend.transaction.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.card.application.dto.CardAdminResponseDto;
import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.wallet.application.dto.WalletAdminResponseDto;

public record TransactionAdminResponseDto(
        UUID id,
        WalletAdminResponseDto wallet,
        String stripeTransactionId,
        String stripeObjectId,
        String type,
        Long amount,
        String currency,
        Long merchantAmount,
        String merchantCurrency,
        String status,
        String direction,
        String bankName,
        String bankAccountLast4,
        String routingNumber,
        PaymentMethod paymentMethod,
        CardAdminResponseDto card,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry,
        LocalDateTime createdAt) {

}
