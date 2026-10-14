package com.jexis.jexis_backend.card.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEphemeralKeyDto(@NotBlank String nonce) {
}
