package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.enums.AccountResource;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.subscription.application.dto.EffectiveSubscriptionEntitlementDto;
import com.jexis.jexis_backend.subscription.application.useCases.GetEffectiveSubscriptionEntitlementsUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckAccountResourceLimitUseCaseTest {
    @Mock
    GetAccountUseCase accounts;
    @Mock
    SubscriptionRepository subscriptions;
    @Mock
    GetEffectiveSubscriptionEntitlementsUseCase entitlements;
    @Mock
    CardRepository cards;
    @Mock
    MemberRepository members;
    @InjectMocks
    CheckAccountResourceLimitUseCase checker;
    UUID accountId = UUID.randomUUID();
    Subscription subscription;

    @BeforeEach
    void setup() {
        subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
    }

    void limit(String value) {
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any())).thenReturn(List.of(subscription));
        when(entitlements.execute(subscription.getId())).thenReturn(List.of(
                new EffectiveSubscriptionEntitlementDto(UUID.randomUUID(), "max_cards", "limit", null,
                        value, EffectiveSubscriptionEntitlementDto.Source.SUBSCRIPTION)));
    }

    @Test
    void eligibilityUsesUtcRegardlessOfServerTimezone() {
        var originalZone = java.util.TimeZone.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Pacific/Honolulu"));
            var before = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
            when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any()))
                    .thenReturn(List.of(subscription));
            checker.requireEligibleSubscription(accountId);
            var timestamp = ArgumentCaptor.forClass(java.time.LocalDateTime.class);
            verify(subscriptions).findEligibleForResourceLimits(eq(accountId), timestamp.capture(), any());
            assertThat(timestamp.getValue()).isBetween(before, java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        } finally {
            java.util.TimeZone.setDefault(originalZone);
        }
    }

    @Test
    void checksExactBoundaryAndAvoidsOverflow() {
        limit("10");
        when(cards.countResourceUsage(eq(accountId), any())).thenReturn(8L);
        assertThat(checker.execute(accountId, AccountResource.CARDS, 2).allowed()).isTrue();
        var denied = checker.execute(accountId, AccountResource.CARDS, 3);
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.remaining()).isEqualTo(2);
        assertThat(denied.reason()).isEqualTo("LIMIT_EXCEEDED");
        assertThat(checker.execute(accountId, AccountResource.CARDS, Long.MAX_VALUE).allowed()).isFalse();
        verify(cards, times(3)).countResourceUsage(accountId, List.of(CardStatus.active, CardStatus.inactive));
    }

    @Test
    void zeroIsFiniteAndQuantityZeroChecksCurrentUsage() {
        limit("0");
        assertThat(checker.execute(accountId, AccountResource.CARDS, 0).allowed()).isTrue();
        assertThat(checker.execute(accountId, AccountResource.CARDS, 1).allowed()).isFalse();
        when(cards.countResourceUsage(eq(accountId), any())).thenReturn(1L);
        assertThat(checker.execute(accountId, AccountResource.CARDS, 0).allowed()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "unlimited", "1.5", "10GB", "", " 10", "9223372036854775808"})
    void rejectsInvalidValues(String value) {
        limit(value);
        assertThat(checker.execute(accountId, AccountResource.CARDS, 1).reason())
                .isEqualTo("INVALID_LIMIT_CONFIGURATION");
    }

    @Test
    void missingLimitDeniesAdditions() {
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any())).thenReturn(List.of(subscription));
        when(entitlements.execute(subscription.getId())).thenReturn(List.of());
        assertThat(checker.execute(accountId, AccountResource.CARDS, 1).reason()).isEqualTo("LIMIT_NOT_CONFIGURED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"limit", "LIMIT", "custom_category"})
    void categoryDoesNotRestrictNumericLimit(String category) {
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any())).thenReturn(List.of(subscription));
        when(cards.countResourceUsage(eq(accountId), any())).thenReturn(4L);
        when(entitlements.execute(subscription.getId())).thenReturn(List.of(
                new EffectiveSubscriptionEntitlementDto(UUID.randomUUID(), "max_cards", category, null,
                        "5", EffectiveSubscriptionEntitlementDto.Source.SUBSCRIPTION)));
        assertThat(checker.execute(accountId, AccountResource.CARDS, 1).allowed()).isTrue();
        assertThat(checker.execute(accountId, AccountResource.CARDS, 2).reason()).isEqualTo("LIMIT_EXCEEDED");
    }

    @Test
    void noSubscriptionDeniesAndMemberUsageIsAccountScoped() {
        when(members.countByAccountId(accountId)).thenReturn(2L);
        var result = checker.execute(accountId, AccountResource.MEMBERS, 1);
        assertThat(result.used()).isEqualTo(2);
        assertThat(result.reason()).isEqualTo("NO_ELIGIBLE_SUBSCRIPTION");
        verifyNoInteractions(entitlements, cards);
        assertThatThrownBy(() -> checker.requireCapacity(accountId, AccountResource.MEMBERS, 1))
                .isInstanceOf(ResourceLimitException.class);
    }

    @Test
    void invalidQuantityDoesNotQueryDatabase() {
        assertThatThrownBy(() -> checker.execute(accountId, AccountResource.CARDS, -1))
                .isInstanceOf(ResourceLimitException.class);
        verifyNoInteractions(accounts, subscriptions, cards, members);
    }
}
