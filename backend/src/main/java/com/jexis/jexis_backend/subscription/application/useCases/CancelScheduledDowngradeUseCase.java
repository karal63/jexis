package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CancelStripeScheduledSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelScheduledDowngradeUseCase {
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final CancelStripeScheduledSubscriptionUseCase cancelStripeScheduledSubscriptionUseCase;
    private final SubscriptionRepository subscriptionRepository;

    public void execute(UUID subscriptionId) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);
        if (subscription.getStripeSubscriptionId() == null) {
            throw new LocalSubscriptionOperationException();
        }
        cancelStripeScheduledSubscriptionUseCase.execute(subscription.getStripeSubscriptionId());
        subscription.setScheduledPlan(null);
        subscription.setStripeScheduleId(null);
        subscriptionRepository.save(subscription);
    }
}
