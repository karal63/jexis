package com.jexis.jexis_backend.common.dto;

import com.jexis.jexis_backend.card.domain.enums.SpendingInterval;

public class SpendingLimit {
    private Long amount;
    private SpendingInterval interval;

    public Long getAmount() {
        return amount;
    }

    public SpendingInterval getInterval() {
        return interval;
    }
}
