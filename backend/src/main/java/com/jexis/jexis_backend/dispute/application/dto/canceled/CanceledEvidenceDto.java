package com.jexis.jexis_backend.dispute.application.dto.canceled;

import com.jexis.jexis_backend.dispute.application.dto.ProductType;
import com.jexis.jexis_backend.dispute.application.dto.ReturnStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CanceledEvidenceDto(
        String additionalDocumentation,
        @NotNull() Long canceledAt,
        @NotNull() Boolean cancellationPolicyProvided,
        @NotNull() String cancellationReason,
        @NotNull() Long expectedAt,
        @NotNull() String explanation,
        @NotNull() String productDescription,
        @NotNull() ProductType productType,
        ReturnStatus returnStatus,
        Long returnedAt
) {
}
