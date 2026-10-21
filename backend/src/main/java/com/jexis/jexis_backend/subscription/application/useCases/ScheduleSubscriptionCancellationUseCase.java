package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CancelStripeSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleSubscriptionCancellationUseCase {
    private final CancelStripeSubscriptionUseCase cancelStripeSubscriptionUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final SubscriptionRepository subscriptionRepository;

    public void execute(java.util.UUID subscriptionId) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);
        if (subscription.getStripeSubscriptionId() == null) {
            throw new LocalSubscriptionOperationException();
        }

        cancelStripeSubscriptionUseCase.execute(subscription.getStripeSubscriptionId());

        subscription.setCancelAtPeriodEnd(true);
        subscriptionRepository.save(subscription);
    }
}
