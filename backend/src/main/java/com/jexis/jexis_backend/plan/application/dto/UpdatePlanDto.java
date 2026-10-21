package com.jexis.jexis_backend.plan.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdatePlanDto {
    private String name;

    private String code;

    private String description;
}
