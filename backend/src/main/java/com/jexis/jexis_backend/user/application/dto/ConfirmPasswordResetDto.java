package com.jexis.jexis_backend.user.application.dto;

import com.jexis.jexis_backend.common.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPasswordResetDto(
        @NotBlank String token,
        @ValidPassword String password
        ) {
}
