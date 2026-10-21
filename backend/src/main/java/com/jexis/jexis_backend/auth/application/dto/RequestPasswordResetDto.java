package com.jexis.jexis_backend.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RequestPasswordResetDto(
        @NotNull @Email String email
) {
}
