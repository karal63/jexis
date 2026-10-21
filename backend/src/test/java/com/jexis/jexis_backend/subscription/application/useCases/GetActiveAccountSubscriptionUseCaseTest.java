package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActiveAccountSubscriptionUseCaseTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @InjectMocks
    private GetActiveAccountSubscriptionUseCase useCase;

    @Test
    void returnsNullWhenAccountHasNoActiveSubscription() {
        UUID accountId = UUID.randomUUID();
        when(subscriptionRepository.findActiveSubscription(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThat(useCase.execute(accountId)).isNull();
    }

    @Test
    void returnsActiveSubscriptionWhenPresent() {
        UUID accountId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        when(subscriptionRepository.findActiveSubscription(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(subscription));

        assertThat(useCase.execute(accountId)).isSameAs(subscription);
    }
}
