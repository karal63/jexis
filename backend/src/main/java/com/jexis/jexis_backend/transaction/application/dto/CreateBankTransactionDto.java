package com.jexis.jexis_backend.transaction.application.dto;

import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;

public record CreateBankTransactionDto(
        String accountId,
        String transactionId,
        String financialAccountId,
        String stripeObjectId,
        TransactionType type,
        Long amount,
        String currency,
        TransactionStatus status,
        TransactionDirection direction,
        String bankName,
        String bankAccountLast4,
        String routingNumber,
        PaymentMethod paymentMethod
) {

}
