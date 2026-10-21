package com.jexis.jexis_backend.transaction.domain.enums;

public enum TransactionType {
    INBOUND_TRANSFER,
    OUTBOUND_TRANSFER,
    CARD_AUTHORIZATION,
    CARD_TRANSACTION,
    CARD_REFUND,
    CARD_REVERSAL,
    FEE,
    ADJUSTMENT
}
