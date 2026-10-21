package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.subscription.infrastructure.stripe.SubscriptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class CancelSubscriptionUseCase {
    private final GetSubscriptionByStripeIdUseCase getSubscriptionByStripeIdUseCase;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionRepository subscriptionRepository;

    public void execute(com.stripe.model.Subscription stripeSubscription) {
        LocalDateTime convertedCanceledAt = stripeSubscription.getCanceledAt() != null ? Instant
                .ofEpochSecond(stripeSubscription.getCanceledAt())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime() : null;

        Subscription subscription = getSubscriptionByStripeIdUseCase.execute(stripeSubscription.getId());

        subscription.setStatus(subscriptionMapper.mapSubscriptionStatus(stripeSubscription.getStatus()));
        subscription.setCanceledAt(convertedCanceledAt);

        subscriptionRepository.save(subscription);
    }
}
