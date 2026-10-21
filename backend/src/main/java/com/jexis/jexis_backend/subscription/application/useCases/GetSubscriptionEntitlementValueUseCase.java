package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.PlanEntitlementNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionEntitlementRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSubscriptionEntitlementValueUseCase {
    private final SubscriptionEntitlementRepository subscriptionEntitlementRepository;
    private final PlanEntitlementRepository planEntitlementRepository;
    private final GetSubscriptionUseCase getSubscriptionUseCase;

    @Transactional(readOnly = true)
    public String execute(UUID subscriptionId, UUID entitlementId) {
        return subscriptionEntitlementRepository.findBySubscriptionIdAndEntitlementId(subscriptionId, entitlementId)
                .map(subscriptionEntitlement -> subscriptionEntitlement.getValue())
                .orElseGet(() -> {
                    var subscription = getSubscriptionUseCase.execute(subscriptionId);
                    return planEntitlementRepository
                            .findByPlanIdAndEntitlementId(subscription.getPlan().getId(), entitlementId)
                            .map(planEntitlement -> planEntitlement.getValue())
                            .orElseThrow(PlanEntitlementNotFoundException::new);
                });
    }
}
