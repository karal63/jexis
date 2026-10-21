package com.jexis.jexis_backend.plan.application.dto;

import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class PlanResponseDto {
    private final UUID id;
    private final String name;
    private final String code;
    private final String description;
    private final boolean isActive;
    private final PlanStatus status;
    private final boolean canPublish;
    private final LocalDateTime createdAt;
    private final Price defaultPrice;
}
