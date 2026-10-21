package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanCannotBePublishedException;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublishPlanUseCase {
    private final GetPlanUseCase getPlanUseCase;
    private final PlanRepository planRepository;

    @Transactional
    public Plan execute(UUID planId) {
        Plan plan = getPlanUseCase.execute(planId);

        if (!plan.isPublishable()) {
            throw new PlanCannotBePublishedException();
        }

        plan.setStatus(PlanStatus.PUBLISHED);
        plan.setActive(true);
        return planRepository.save(plan);
    }
}
