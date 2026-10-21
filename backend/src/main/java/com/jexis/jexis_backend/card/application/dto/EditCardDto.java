package com.jexis.jexis_backend.card.application.dto;

import java.util.List;

import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import jakarta.validation.Valid;

/**
 * EditCardDto
 *
 * Data Transfer Object used for card editing requests. It encapsulates the
 * necessary data required to edit an existing card, such as the card details.
 *
 * Author: Leo
 */
public record EditCardDto(
        CardStatus status,
        @Valid
        List<SpendingLimit> spendingLimits) {
}
