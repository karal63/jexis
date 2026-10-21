package com.jexis.jexis_backend.dispute.application.dto.other;

import com.jexis.jexis_backend.dispute.application.dto.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtherEvidenceDto(
        String additionalDocumentation,
        @NotNull String explanation,
        @NotNull String productDescription,
        @NotNull ProductType productType
) {
}
