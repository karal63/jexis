package com.jexis.jexis_backend.user.application.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmPasswordResetDto(
        @NotNull String token,
        @NotNull String password
        ) {
}
