package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.application.useCases.*;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.entities.PendingSubscriptionOperation;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.subscription.infrastructure.PendingSubscriptionOperationRepository;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CancelDuplicateStripeSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.infrastructure.stripe.SubscriptionMapper;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncFreeSubscriptionTest {
    @Mock SubscriptionRepository repository;
    @Mock GetUserUseCase users;
    @Mock GetAccountUseCase accounts;
    @Mock GetPlanUseCase plans;
    @Mock GetPriceByStripeIdUseCase prices;
    @Mock GetPlanByStripeIdUseCase stripePlans;
    @Mock SubscriptionMapper mapper;
    @Mock LockAccountUseCase lockAccount;
    @Mock PendingSubscriptionOperationRepository pendingOperations;
    @Mock CancelDuplicateStripeSubscriptionUseCase cancelDuplicate;
    @InjectMocks SyncSubscriptionUseCase sync;

    @ParameterizedTest
    @EnumSource(value = SubscriptionStatus.class, names = {"ACTIVE", "TRIALING", "INCOMPLETE"})
    void retiresFreeSubscriptionOnlyWhenPaidAccessIsConfirmed(SubscriptionStatus status) {
        UUID userId = UUID.randomUUID(), accountId = UUID.randomUUID();
        Account account = new Account(); account.setId(accountId);
        var stripe = mock(com.stripe.model.Subscription.class, Answers.RETURNS_DEEP_STUBS);
        when(stripe.getId()).thenReturn("sub_paid");
        when(stripe.getMetadata()).thenReturn(Map.of("userId", userId.toString(), "accountId", accountId.toString()));
        when(stripe.getStatus()).thenReturn(status.name().toLowerCase());
        when(mapper.mapSubscriptionStatus(stripe.getStatus())).thenReturn(status);
        when(users.execute(userId)).thenReturn(new User());
        when(accounts.execute(accountId)).thenReturn(account);
        when(pendingOperations.findByAccountId(accountId)).thenReturn(java.util.Optional.empty());
        when(repository.findNonTerminalPaidSubscriptions(accountId)).thenReturn(List.of());
        var item = stripe.getItems().getData().get(0);
        when(item.getCurrentPeriodStart()).thenReturn(1700000000L);
        when(item.getCurrentPeriodEnd()).thenReturn(1702600000L);
        when(item.getPrice().getId()).thenReturn("price_paid");
        when(item.getPlan().getProduct()).thenReturn("prod_paid");
        when(prices.execute("price_paid")).thenReturn(new Price());
        when(stripePlans.execute("prod_paid")).thenReturn(new Plan());
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        Subscription free = new Subscription(); free.setStatus(SubscriptionStatus.ACTIVE);
        if (status != SubscriptionStatus.INCOMPLETE) {
            when(repository.findByAccountIdAndStripeSubscriptionIdIsNullAndStatus(accountId, SubscriptionStatus.ACTIVE))
                    .thenReturn(List.of(free));
        }

        assertThat(sync.execute(stripe).getStripeSubscriptionId()).isEqualTo("sub_paid");

        if (status == SubscriptionStatus.INCOMPLETE) {
            assertThat(free.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            verify(repository, never()).save(free);
        } else {
            assertThat(free.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
            assertThat(free.getCurrentPeriodEnd()).isEqualTo(free.getCanceledAt()).isNotNull();
            verify(repository).save(free);
        }
    }

    @Test
    void immediatelyCancelsAProviderSubscriptionThatIsNotCanonicalForTheAccount() {
        UUID userId = UUID.randomUUID(), accountId = UUID.randomUUID();
        Account account = new Account(); account.setId(accountId);
        PendingSubscriptionOperation pending = new PendingSubscriptionOperation();
        pending.setStripeSubscriptionId("sub_canonical");
        Subscription canonical = new Subscription();
        canonical.setStripeSubscriptionId("sub_canonical");
        var duplicate = mock(com.stripe.model.Subscription.class, Answers.RETURNS_DEEP_STUBS);
        when(duplicate.getId()).thenReturn("sub_duplicate");
        when(duplicate.getMetadata()).thenReturn(Map.of(
                "userId", userId.toString(), "accountId", accountId.toString()));
        when(duplicate.getStatus()).thenReturn("canceled");
        when(mapper.mapSubscriptionStatus("canceled")).thenReturn(SubscriptionStatus.CANCELED);
        when(pendingOperations.findByAccountId(accountId)).thenReturn(java.util.Optional.of(pending));
        when(repository.findNonTerminalPaidSubscriptions(accountId)).thenReturn(List.of(canonical));
        when(cancelDuplicate.execute("sub_duplicate")).thenReturn(duplicate);
        when(users.execute(userId)).thenReturn(new User());
        when(accounts.execute(accountId)).thenReturn(account);
        var item = duplicate.getItems().getData().get(0);
        when(item.getCurrentPeriodStart()).thenReturn(1700000000L);
        when(item.getCurrentPeriodEnd()).thenReturn(1702600000L);
        when(item.getPrice().getId()).thenReturn("price_paid");
        when(item.getPlan().getProduct()).thenReturn("prod_paid");
        when(prices.execute("price_paid")).thenReturn(new Price());
        when(stripePlans.execute("prod_paid")).thenReturn(new Plan());
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));

        Subscription saved = sync.execute(duplicate);

        assertThat(saved.getStripeSubscriptionId()).isEqualTo("sub_duplicate");
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        verify(cancelDuplicate).execute("sub_duplicate");
        verify(repository, never()).save(canonical);
        verify(pendingOperations, never()).delete(any());
    }
}
