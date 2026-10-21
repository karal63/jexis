package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.exceptions.SubscriptionNotFoundException;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSubscriptionByStripeIdUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public Subscription execute(String stripeId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeId)
                .orElseThrow(SubscriptionNotFoundException::new);
    }
}
