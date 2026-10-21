package com.jexis.jexis_backend.plan.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SetDefaultPriceDto(
        @NotNull UUID priceId
) {
}
