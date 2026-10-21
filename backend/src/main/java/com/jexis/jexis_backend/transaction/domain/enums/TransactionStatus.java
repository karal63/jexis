package com.jexis.jexis_backend.transaction.domain.enums;

public enum TransactionStatus {
    COMPLETED(20),
    FAILED(20),
    CANCELED(20),
    PROCESSING(10);

    private final int priority;

    TransactionStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}