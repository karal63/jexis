package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetActiveAccountSubscriptionUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public Subscription execute(UUID accountId) {
        return subscriptionRepository.findActiveSubscription(
                accountId,
                LocalDateTime.now()
        ).orElse(null);
    }
}
