package com.jexis.jexis_backend.card.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateEphemeralKeyDto(@NotNull String nonce) {
}
