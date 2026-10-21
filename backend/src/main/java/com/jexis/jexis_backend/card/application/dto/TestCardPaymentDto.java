package com.jexis.jexis_backend.card.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TestCardPaymentDto(
        @NotNull(message = "Card id cannot be blank")
        UUID cardId,

        @NotNull(message = "Amount cannot be blank")
        Long amount,

        @Size(min = 3, max = 3)
        String currency
) {
}
