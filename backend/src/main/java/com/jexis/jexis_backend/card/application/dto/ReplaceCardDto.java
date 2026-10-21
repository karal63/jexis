package com.jexis.jexis_backend.card.application.dto;

import com.jexis.jexis_backend.card.domain.enums.CardReplacementReason;

import jakarta.validation.constraints.NotNull;

public record ReplaceCardDto(
        @NotNull(message = "Replacement reason cannot be blank")
        CardReplacementReason replacementReason) {
}
