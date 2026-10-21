package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotActivatedException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFreeSubscriptionUseCaseTest {
    @Mock PlanRepository plans;
    @Mock SubscriptionRepository subscriptions;
    @Mock LockAccountUseCase lock;
    @InjectMocks CreateFreeSubscriptionUseCase create;
    Account account;

    @BeforeEach void setup() {
        User owner = new User();
        owner.setIsActivated(true);
        account = new Account();
        account.setId(UUID.randomUUID());
        account.setOwner(owner);
        when(lock.execute(account.getId())).thenReturn(account);
    }

    @Test void createsActiveLocalSubscriptionWithoutExpirationOrPaymentDetails() {
        Plan plan = new Plan(null, "Free", "free", null, true, PlanStatus.PUBLISHED);
        Price price = new Price(plan, null, "usd", 0L, "month", 1, true);
        plan.setDefaultPrice(price);
        when(plans.findByCode("free")).thenReturn(Optional.of(plan));
        when(subscriptions.save(any())).thenAnswer(call -> call.getArgument(0));

        Subscription result = create.execute(account);

        assertThat(result.getUser()).isSameAs(account.getOwner());
        assertThat(result.getAccount()).isSameAs(account);
        assertThat(result.getPlan()).isSameAs(plan);
        assertThat(result.getPrice()).isSameAs(price);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.getCurrentPeriodStart()).isNotNull();
        assertThat(result.getCurrentPeriodEnd()).isNull();
        assertThat(result.getStripeSubscriptionId()).isNull();
        assertThat(result.getStripePaymentMethodId()).isNull();
        assertThat(result.isCancelAtPeriodEnd()).isFalse();
    }

    @Test void repeatedProvisioningPreservesExistingSubscription() {
        Subscription existing = new Subscription();
        when(subscriptions.findActiveSubscription(eq(account.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existing));
        assertThat(create.execute(account)).isSameAs(existing);
        verify(subscriptions, never()).save(any());
        verifyNoInteractions(plans);
    }

    @Test void rejectsUnverifiedOwner() {
        account.getOwner().setIsActivated(false);
        assertThatThrownBy(() -> create.execute(account)).isInstanceOf(UserNotActivatedException.class);
        verifyNoInteractions(plans, subscriptions);
    }

    @Test void refusesPaidPriceOnFreePlan() {
        Plan plan = new Plan(null, "Free", "free", null, true, PlanStatus.PUBLISHED);
        plan.setDefaultPrice(new Price(plan, null, "usd", 100L, "month", 1, true));
        when(plans.findByCode("free")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> create.execute(account)).isInstanceOf(IllegalStateException.class);
        verify(subscriptions, never()).save(any());
    }
}
