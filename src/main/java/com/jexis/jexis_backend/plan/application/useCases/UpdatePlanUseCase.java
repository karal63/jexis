package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import com.jexis.jexis_backend.plan.application.dto.UpdatePlanDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.product.UpdateStripeProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdatePlanUseCase {
    private final PlanRepository planRepository;
    private final GetPlanUseCase getPlanUseCase;
    private final UpdateStripeProductUseCase updateStripeProductUseCase;

    @Transactional
    public Plan execute(UUID id, UpdatePlanDto dto) {
        boolean changed = false;
        Plan plan = getPlanUseCase.execute(id);

        if (dto.getCode() != null && !dto.getCode().equals(plan.getCode())
                && (plan.isFreePlan() || Plan.FREE_CODE.equals(dto.getCode()))) {
            throw new ProtectedFreePlanException();
        }

        if (dto.getName() != null) {
            plan.setName(dto.getName());
            changed = true;
        }

        if (dto.getDescription() != null) {
            plan.setDescription(dto.getDescription());
            changed = true;
        }

        if (dto.getCode() != null) {
            plan.setCode(dto.getCode());
            changed = true;
        }

        if (changed && plan.getStripePlanId() != null) {
            updateStripeProductUseCase.execute(plan.getStripePlanId(), dto);
        }

        return planRepository.save(plan);
    }
}
