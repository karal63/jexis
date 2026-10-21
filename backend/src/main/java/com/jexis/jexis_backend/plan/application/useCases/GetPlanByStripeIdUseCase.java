package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPlanByStripeIdUseCase {
    private final PlanRepository planRepository;

    public Plan execute(String stripePlanId) {
        return planRepository.findByStripePlanId(stripePlanId).orElseThrow(PlanNotFoundException::new);
    }
}
