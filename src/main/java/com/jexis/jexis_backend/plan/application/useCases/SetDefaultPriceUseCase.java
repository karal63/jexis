package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import com.jexis.jexis_backend.plan.application.dto.SetDefaultPriceDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.SetStripeDefaultPriceUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SetDefaultPriceUseCase {
    private final GetPlanUseCase getPlanUseCase;
    private final PlanRepository planRepository;
    private final GetPlanPriceUseCase getPlanPriceUseCase;
    private final SetStripeDefaultPriceUseCase setStripeDefaultPriceUseCase;

    @Transactional
    public void execute(UUID planId, SetDefaultPriceDto dto) {
        Plan plan = getPlanUseCase.execute(planId);
        if (plan.isFreePlan()) {
            throw new ProtectedFreePlanException();
        }
        if (plan.getStripePlanId() == null) {
            throw new LocalSubscriptionOperationException();
        }
        Price price = getPlanPriceUseCase.execute(dto.priceId());
        plan.setDefaultPrice(price);
        planRepository.save(plan);

        setStripeDefaultPriceUseCase.execute(plan.getStripePlanId(), price.getStripePriceId());
    }
}
