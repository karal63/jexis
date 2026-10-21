package com.jexis.jexis_backend.transaction.application.dto;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTransactionDto(
        UUID walletId,
        String stripeObjectId,
        TransactionType type,
        Long amount,
        String currency,
        TransactionStatus status,
        TransactionDirection direction
//        String bankName,
//        String bankAccountLast4,
//        String routingNumber,
//        PaymentMethod paymentMethod,
//        Card card,
//        String merchantName,
//        String merchantCategory,
//        String merchantCity,
//        String merchantCountry,
//        String transactionId
) {
}
