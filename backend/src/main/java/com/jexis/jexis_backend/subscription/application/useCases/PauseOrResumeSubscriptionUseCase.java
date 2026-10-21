package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.subscription.infrastructure.stripe.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PauseOrResumeSubscriptionUseCase {
    private final GetSubscriptionByStripeIdUseCase getSubscriptionByStripeIdUseCase;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionRepository subscriptionRepository;

    public void execute(com.stripe.model.Subscription stripeSubscription) {
        Subscription subscription = getSubscriptionByStripeIdUseCase.execute(stripeSubscription.getId());
        subscription.setStatus(subscriptionMapper.mapSubscriptionStatus(stripeSubscription.getStatus()));
        subscriptionRepository.save(subscription);
    }
}
