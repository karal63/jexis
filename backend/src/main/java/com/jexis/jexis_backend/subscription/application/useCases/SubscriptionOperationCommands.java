package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.PendingSubscriptionOperation;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType;
import com.jexis.jexis_backend.subscription.domain.exceptions.PendingSubscriptionOperationConflictException;
import com.jexis.jexis_backend.subscription.infrastructure.PendingSubscriptionOperationRepository;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionOperationCommands {
    private final PendingSubscriptionOperationRepository operations;
    private final SubscriptionRepository subscriptions;
    private final LockAccountUseCase lockAccount;

    public record Preparation(Subscription activePaidSubscription,
                              PendingSubscriptionOperation operation) {}

    // Commit the operation identity before Stripe is contacted, so every retry
    // uses the same provider idempotency key even if a response is lost.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Preparation prepare(UUID accountId, UUID userId, UUID planId,
            SubscriptionOperationType type, String paymentMethodId) {
        lockAccount.execute(accountId);

        Subscription active = subscriptions
                .findActiveSubscription(accountId, LocalDateTime.now(ZoneOffset.UTC))
                .filter(subscription -> subscription.getStripeSubscriptionId() != null)
                .orElse(null);
        if (active != null) {
            return new Preparation(active, null);
        }

        PendingSubscriptionOperation operation = operations.findByAccountId(accountId).orElse(null);
        if (operation != null) {
            if (!operation.getUserId().equals(userId)
                    || !operation.getPlanId().equals(planId)
                    || operation.getType() != type
                    || !Objects.equals(operation.getPaymentMethodId(), paymentMethodId)) {
                throw new PendingSubscriptionOperationConflictException(
                        "SUBSCRIPTION_OPERATION_IN_PROGRESS",
                        "A different subscription operation is already in progress for this account");
            }
            if (!operation.getCreatedAt().plus(Duration.ofHours(23)).isAfter(Instant.now())) {
                throw new PendingSubscriptionOperationConflictException(
                        "SUBSCRIPTION_RECONCILIATION_REQUIRED",
                        "The previous subscription outcome is unknown and must be reconciled with Stripe");
            }
            return new Preparation(null, operation);
        }

        operation = new PendingSubscriptionOperation();
        operation.setId(UUID.randomUUID());
        operation.setAccountId(accountId);
        operation.setUserId(userId);
        operation.setPlanId(planId);
        operation.setType(type);
        operation.setPaymentMethodId(paymentMethodId);
        operation.setCreatedAt(Instant.now());
        return new Preparation(null, operations.save(operation));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeCheckout(UUID operationId, String checkoutUrl) {
        operations.findById(operationId).ifPresent(operation -> operation.setCheckoutUrl(checkoutUrl));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeDirect(UUID operationId, String stripeSubscriptionId) {
        operations.findById(operationId)
                .ifPresent(operation -> operation.setStripeSubscriptionId(stripeSubscriptionId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void abandon(UUID operationId) {
        operations.findById(operationId).ifPresent(operation -> {
            if (operation.getCheckoutUrl() == null && operation.getStripeSubscriptionId() == null) {
                operations.delete(operation);
            }
        });
    }
}
