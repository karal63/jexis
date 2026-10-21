package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.product.DeleteStripeProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletePlanUseCase {
    private final DeleteStripeProductUseCase deleteStripeProductUseCase;
    private final PlanRepository planRepository;
    private final GetPlanUseCase getPlanUseCase;

    @Transactional
    public void execute(UUID planId) {
        Plan plan = getPlanUseCase.execute(planId);
        if (plan.isFreePlan()) {
            throw new ProtectedFreePlanException();
        }
        if (plan.getStripePlanId() == null) {
            throw new LocalSubscriptionOperationException();
        }
        planRepository.delete(plan);
        deleteStripeProductUseCase.execute(plan.getStripePlanId());
    }
}
