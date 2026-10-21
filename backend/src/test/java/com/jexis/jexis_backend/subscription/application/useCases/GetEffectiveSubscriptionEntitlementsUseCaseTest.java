package com.jexis.jexis_backend.subscription.application.useCases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import com.jexis.jexis_backend.subscription.application.dto.EffectiveSubscriptionEntitlementDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionEntitlementRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetEffectiveSubscriptionEntitlementsUseCaseTest {
    @Mock
    private GetSubscriptionUseCase getSubscriptionUseCase;
    @Mock
    private PlanEntitlementRepository planEntitlementRepository;
    @Mock
    private SubscriptionEntitlementRepository subscriptionEntitlementRepository;
    @InjectMocks
    private GetEffectiveSubscriptionEntitlementsUseCase useCase;

    @Test
    void executeCombinesPlanEntitlementsAndSubscriptionOverrides() {
        UUID subscriptionId = UUID.randomUUID();
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setPlan(plan);

        Entitlement seats = entitlement("seats", "NUMBER");
        Entitlement exports = entitlement("exports", "BOOLEAN");
        Entitlement customBranding = entitlement("custom_branding", "BOOLEAN");

        when(getSubscriptionUseCase.execute(subscriptionId)).thenReturn(subscription);
        when(planEntitlementRepository.findByPlanId(plan.getId())).thenReturn(List.of(
                new PlanEntitlement(plan, seats, "5"),
                new PlanEntitlement(plan, exports, "true")
        ));
        when(subscriptionEntitlementRepository.findBySubscriptionId(subscriptionId)).thenReturn(List.of(
                new SubscriptionEntitlement(subscription, seats, "20"),
                new SubscriptionEntitlement(subscription, customBranding, "true")
        ));

        List<EffectiveSubscriptionEntitlementDto> result = useCase.execute(subscriptionId);

        assertThat(result).extracting(
                EffectiveSubscriptionEntitlementDto::key,
                EffectiveSubscriptionEntitlementDto::value,
                EffectiveSubscriptionEntitlementDto::source
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple("seats", "20", EffectiveSubscriptionEntitlementDto.Source.SUBSCRIPTION),
                org.assertj.core.groups.Tuple.tuple("exports", "true", EffectiveSubscriptionEntitlementDto.Source.PLAN),
                org.assertj.core.groups.Tuple.tuple("custom_branding", "true", EffectiveSubscriptionEntitlementDto.Source.SUBSCRIPTION)
        );
        verify(getSubscriptionUseCase).execute(subscriptionId);
        verify(planEntitlementRepository).findByPlanId(plan.getId());
        verify(subscriptionEntitlementRepository).findBySubscriptionId(subscriptionId);
    }

    private Entitlement entitlement(String key, String type) {
        Entitlement entitlement = new Entitlement(key, type, key + " description");
        entitlement.setId(UUID.randomUUID());
        return entitlement;
    }
}
