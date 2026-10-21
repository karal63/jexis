package com.jexis.jexis_backend.cardholder.application.dto;

import java.util.List;

import com.jexis.jexis_backend.common.dto.SpendingLimit;

public record SpendingControlsDto(List<SpendingLimit> spendingLimits) {

}
