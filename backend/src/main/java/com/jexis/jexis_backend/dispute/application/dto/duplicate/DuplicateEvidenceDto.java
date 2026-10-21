package com.jexis.jexis_backend.dispute.application.dto.duplicate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DuplicateEvidenceDto(
        String additionalDocumentation,
        String cardStatement,
        String cashReceipt,
        String checkImage,
        @NotNull String explanation,
        String originalTransaction
) {
}
