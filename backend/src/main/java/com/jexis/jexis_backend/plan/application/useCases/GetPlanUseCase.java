package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlanUseCase {
    private final PlanRepository planRepository;

    public Plan execute(UUID planId) {
        return planRepository.findById(planId).orElseThrow(PlanNotFoundException::new);
    }
}
