package com.jexis.jexis_backend.user.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActivateUserDto(
        @NotNull UUID userId
        ) {
}
