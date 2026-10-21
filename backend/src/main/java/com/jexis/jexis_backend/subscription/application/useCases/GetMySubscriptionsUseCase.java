package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMySubscriptionsUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public Page<Subscription> execute(UUID userId, int page, int pageSize) {
        return subscriptionRepository.findByUserId(
                userId,
                PageRequest.of(Math.max(0, page - 1), pageSize)
        );
    }
}
