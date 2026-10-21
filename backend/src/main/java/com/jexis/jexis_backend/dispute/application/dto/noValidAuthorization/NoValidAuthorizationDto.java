package com.jexis.jexis_backend.dispute.application.dto.noValidAuthorization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NoValidAuthorizationDto(
        String additionalDocumentation,
        @NotNull String explanation
) {
}
