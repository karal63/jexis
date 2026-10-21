package com.jexis.jexis_backend.card.application.dto;

import com.jexis.jexis_backend.card.domain.enums.SpendingInterval;

public record SpendingLimitDto(
        int amount,
        SpendingInterval interval) {

}
