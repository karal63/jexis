package com.jexis.jexis_backend.transaction.application.dto;

import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;

public record CreateCardTransactionDto(
        String accountId,
        String debitTransactionId,
        String stripeObjectId,
        TransactionType type,
        Long amount,
        String currency,
        Long merchantAmount,
        String merchantCurrency,
        TransactionStatus status,
        TransactionDirection direction,
        String cardId,
        String authorizationId,
        String merchantName,
        String merchantCategory,
        String merchantCity,
        String merchantCountry
) {

}
