package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionEntitlementRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSubscriptionEntitlementsUseCase {
    private final SubscriptionEntitlementRepository subscriptionEntitlementRepository;
    private final GetSubscriptionUseCase getSubscriptionUseCase;

    @Transactional(readOnly = true)
    public List<SubscriptionEntitlement> execute(UUID subscriptionId) {
        getSubscriptionUseCase.execute(subscriptionId);
        return subscriptionEntitlementRepository.findBySubscriptionId(subscriptionId);
    }
}
