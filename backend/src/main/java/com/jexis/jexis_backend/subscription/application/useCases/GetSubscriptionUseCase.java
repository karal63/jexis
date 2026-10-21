package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.exceptions.SubscriptionNotFoundException;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSubscriptionUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public Subscription execute(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId).orElseThrow(SubscriptionNotFoundException::new);
    }
}
