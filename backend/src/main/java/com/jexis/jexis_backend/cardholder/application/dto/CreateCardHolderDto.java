package com.jexis.jexis_backend.cardholder.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCardHolderDto(
        @NotNull UUID accountId,
        @NotNull UUID userId,
        @NotBlank String addressLine1,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        @NotBlank String postalCode) {
}
