package com.jexis.jexis_backend.plan.presentation;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.plan.application.dto.PlanResponseDto;
import com.jexis.jexis_backend.plan.application.useCases.GetPublicPlanUseCase;
import com.jexis.jexis_backend.plan.application.useCases.GetPublicPlansUseCase;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublicPlanController {
    private final GetPublicPlansUseCase getPublicPlansUseCase;
    private final GetPublicPlanUseCase getPublicPlanUseCase;
    private final DtoHelper dtoHelper;

    @GetMapping("/plans")
    public List<PlanResponseDto> list() {
        List<Plan> plans = getPublicPlansUseCase.execute();
        return plans.stream().map(dtoHelper::toPlanDto).toList();
    }

    @GetMapping("/plans/{id}")
    public PlanResponseDto get(@PathVariable UUID id) {
        return dtoHelper.toPlanDto(getPublicPlanUseCase.execute(id));
    }
}
