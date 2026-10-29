package com.jexis.jexis_backend.subscription.application.useCases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.PendingSubscriptionOperation;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType;
import com.jexis.jexis_backend.subscription.domain.exceptions.PendingSubscriptionOperationConflictException;
import com.jexis.jexis_backend.subscription.infrastructure.PendingSubscriptionOperationRepository;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SubscriptionOperationCommandsTest {
    @Test
    void reservesOneStableOperationAndReusesItOnRetry() {
        PendingSubscriptionOperationRepository operations = mock(PendingSubscriptionOperationRepository.class);
        SubscriptionRepository subscriptions = mock(SubscriptionRepository.class);
        LockAccountUseCase lockAccount = mock(LockAccountUseCase.class);
        SubscriptionOperationCommands commands = new SubscriptionOperationCommands(
                operations, subscriptions, lockAccount);
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        AtomicReference<PendingSubscriptionOperation> stored = new AtomicReference<>();
        when(operations.findByAccountId(accountId))
                .thenAnswer(call -> Optional.ofNullable(stored.get()));
        when(operations.save(any())).thenAnswer(call -> {
            PendingSubscriptionOperation operation = call.getArgument(0);
            stored.set(operation);
            return operation;
        });
        when(subscriptions.findActiveSubscription(eq(accountId), any())).thenReturn(Optional.empty());

        var first = commands.prepare(accountId, userId, planId,
                SubscriptionOperationType.CHECKOUT, null).operation();
        var retry = commands.prepare(accountId, userId, planId,
                SubscriptionOperationType.CHECKOUT, null).operation();

        assertThat(retry.getId()).isEqualTo(first.getId());
        verify(lockAccount, times(2)).execute(accountId);
        verify(operations, times(1)).save(any());
    }

    @Test
    void rejectsDifferentParametersWhileAnOperationIsPending() {
        PendingSubscriptionOperationRepository operations = mock(PendingSubscriptionOperationRepository.class);
        SubscriptionRepository subscriptions = mock(SubscriptionRepository.class);
        LockAccountUseCase lockAccount = mock(LockAccountUseCase.class);
        SubscriptionOperationCommands commands = new SubscriptionOperationCommands(
                operations, subscriptions, lockAccount);
        UUID accountId = UUID.randomUUID();
        PendingSubscriptionOperation pending = new PendingSubscriptionOperation();
        pending.setId(UUID.randomUUID());
        pending.setAccountId(accountId);
        pending.setUserId(UUID.randomUUID());
        pending.setPlanId(UUID.randomUUID());
        pending.setType(SubscriptionOperationType.CHECKOUT);
        when(operations.findByAccountId(accountId)).thenReturn(Optional.of(pending));
        when(subscriptions.findActiveSubscription(eq(accountId), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commands.prepare(accountId, pending.getUserId(), UUID.randomUUID(),
                SubscriptionOperationType.CHECKOUT, null))
                .isInstanceOf(PendingSubscriptionOperationConflictException.class)
                .hasMessageContaining("already in progress");
        verify(operations).findByAccountId(accountId);
        verifyNoMoreInteractions(operations);
    }
}
