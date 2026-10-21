package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import com.jexis.jexis_backend.subscription.application.dto.EffectiveSubscriptionEntitlementDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionEntitlementRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetEffectiveSubscriptionEntitlementsUseCase {
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final PlanEntitlementRepository planEntitlementRepository;
    private final SubscriptionEntitlementRepository subscriptionEntitlementRepository;

    @Transactional(readOnly = true)
    public List<EffectiveSubscriptionEntitlementDto> execute(UUID subscriptionId) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);
        Map<UUID, EffectiveSubscriptionEntitlementDto> effectiveEntitlements = new LinkedHashMap<>();

        for (PlanEntitlement planEntitlement
                : planEntitlementRepository.findByPlanId(subscription.getPlan().getId())) {
            Entitlement entitlement = planEntitlement.getEntitlement();
            effectiveEntitlements.put(
                    entitlement.getId(),
                    toDto(entitlement, planEntitlement.getValue(), EffectiveSubscriptionEntitlementDto.Source.PLAN)
            );
        }

        for (SubscriptionEntitlement subscriptionEntitlement
                : subscriptionEntitlementRepository.findBySubscriptionId(subscriptionId)) {
            Entitlement entitlement = subscriptionEntitlement.getEntitlement();
            effectiveEntitlements.put(
                    entitlement.getId(),
                    toDto(
                            entitlement,
                            subscriptionEntitlement.getValue(),
                            EffectiveSubscriptionEntitlementDto.Source.SUBSCRIPTION
                    )
            );
        }

        return List.copyOf(effectiveEntitlements.values());
    }

    private EffectiveSubscriptionEntitlementDto toDto(
            Entitlement entitlement,
            String value,
            EffectiveSubscriptionEntitlementDto.Source source
    ) {
        return new EffectiveSubscriptionEntitlementDto(
                entitlement.getId(),
                entitlement.getKey(),
                entitlement.getType(),
                entitlement.getDescription(),
                value,
                source
        );
    }
}
