package com.jexis.jexis_backend.subscription.application.useCases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllSubscriptionsUseCase {
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<Subscription> execute(int page, int pageSize) {
        return subscriptionRepository.findAll(PageRequest.of(Math.max(0, page - 1), pageSize));
    }
}
