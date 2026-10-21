package com.jexis.jexis_backend.plan.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceDto {
    @NotBlank
    private String currency;

    @NotNull
    @Positive
    private Long unitAmount;

    @NotBlank
    private String interval;

    private Integer intervalCount = 1;

    private boolean active = true;
}
