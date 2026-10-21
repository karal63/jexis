package com.jexis.jexis_backend.dispute.application.dto.notAsDescribed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ServiceNotAsDescribedDto(
        String additionalDocumentation,
        @NotNull Long canceledAt,
        @NotNull String cancellationReason,
        @NotNull String explanation,
        @NotNull Long receivedAt
) {
}
