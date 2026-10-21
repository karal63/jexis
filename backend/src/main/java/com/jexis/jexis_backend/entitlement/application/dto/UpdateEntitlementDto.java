package com.jexis.jexis_backend.entitlement.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEntitlementDto {
    @NotBlank
    private String key;

    @NotBlank
    private String type;

    private String description;
}
