package com.jexis.jexis_backend.dispute.application.dto.notReceived;

import com.jexis.jexis_backend.dispute.application.dto.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotReceivedDto(
        String additionalDocumentation,
        @NotNull Long expectedAt,
        @NotNull String explanation,
        @NotNull String productDescription,
        @NotNull ProductType productType
        ) {
}
