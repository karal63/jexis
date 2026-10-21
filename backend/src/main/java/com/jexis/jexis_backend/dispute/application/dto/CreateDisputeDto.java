package com.jexis.jexis_backend.dispute.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDisputeDto(
        @NotNull()
        UUID accountId,

        @NotNull()
        UUID transactionId,

        @NotNull()
        @Min(0)
        Long amount,

        @NotNull()
        @Valid
        DisputeEvidenceDto disputeEvidence) {
}
