package com.jexis.jexis_backend.wallet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateOutboundTransferDto(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Long amount,

        @NotNull(message = "Currency is required")
        @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a three-letter code")
        String currency,

        @NotNull(message = "Description is temporarily required")
        String description,

        @NotNull(message = "External account id is required")
        UUID externalAccountId
) {
}
