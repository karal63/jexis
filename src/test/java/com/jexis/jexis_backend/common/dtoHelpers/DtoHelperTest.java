package com.jexis.jexis_backend.common.dtoHelpers;

import com.jexis.jexis_backend.subscription.application.dto.SubscriptionResponseDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.user.application.useCases.GetPaymentMethodUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DtoHelperTest {

    @Test
    void walletResponsesIncludeCurrencyAndPreserveLargeMinorUnitAmounts() {
        var account = new com.jexis.jexis_backend.account.domain.entities.Account();
        account.setOwner(new User());
        var wallet = new com.jexis.jexis_backend.wallet.domain.entities.Wallet("Expenses", "fa_test", account);
        wallet.setAvailableBalance(3_000_000_000L);
        var helper = new DtoHelper(mock(GetPaymentMethodUseCase.class));
        var response = helper.toWalletDto(wallet);
        var adminResponse = helper.toWalletAdminDto(wallet);
        assertEquals("USD", response.currency());
        assertEquals(3_000_000_000L, response.availableBalance());
        assertEquals("USD", adminResponse.currency());
        assertEquals(3_000_000_000L, adminResponse.availableBalance());
    }


    @Test
    void optionalPaymentMethodDoesNotCallStripeWhenSubscriptionHasNoPaymentMethod() {
        GetPaymentMethodUseCase getPaymentMethodUseCase = mock(GetPaymentMethodUseCase.class);
        DtoHelper dtoHelper = new DtoHelper(getPaymentMethodUseCase);
        Subscription subscription = new Subscription();
        UUID subscriptionId = UUID.randomUUID();
        subscription.setId(subscriptionId);

        SubscriptionResponseDto result = dtoHelper.toSubscriptionDtoWithOptionalPaymentMethod(subscription);

        assertEquals(subscriptionId, result.id());
        assertNull(result.paymentMethod());
        verifyNoInteractions(getPaymentMethodUseCase);
    }

    @Test
    void optionalPaymentMethodReturnsPaymentMethodWhenItExists() {
        GetPaymentMethodUseCase getPaymentMethodUseCase = mock(GetPaymentMethodUseCase.class);
        DtoHelper dtoHelper = new DtoHelper(getPaymentMethodUseCase);
        Subscription subscription = subscriptionWithPaymentMethod("cus_123", "pm_123");
        PaymentMethod paymentMethod = mock(PaymentMethod.class, RETURNS_DEEP_STUBS);
        when(paymentMethod.getId()).thenReturn("pm_123");
        when(paymentMethod.getCard().getBrand()).thenReturn("visa");
        when(paymentMethod.getCard().getLast4()).thenReturn("4242");
        when(paymentMethod.getCard().getExpMonth()).thenReturn(12L);
        when(paymentMethod.getCard().getExpYear()).thenReturn(2030L);
        when(getPaymentMethodUseCase.execute("cus_123", "pm_123")).thenReturn(paymentMethod);

        SubscriptionResponseDto result = dtoHelper.toSubscriptionDtoWithOptionalPaymentMethod(subscription);

        assertEquals("pm_123", result.paymentMethod().id());
        assertEquals("4242", result.paymentMethod().last4());
        verify(getPaymentMethodUseCase).execute("cus_123", "pm_123");
    }

    @Test
    void optionalPaymentMethodReturnsNullWhenStripeRecordIsStale() {
        GetPaymentMethodUseCase getPaymentMethodUseCase = mock(GetPaymentMethodUseCase.class);
        DtoHelper dtoHelper = new DtoHelper(getPaymentMethodUseCase);
        Subscription subscription = subscriptionWithPaymentMethod("cus_123", "pm_stale");
        when(getPaymentMethodUseCase.execute("cus_123", "pm_stale"))
                .thenThrow(new RuntimeException("Stripe lookup failed"));

        SubscriptionResponseDto result = dtoHelper.toSubscriptionDtoWithOptionalPaymentMethod(subscription);

        assertNull(result.paymentMethod());
        verify(getPaymentMethodUseCase).execute("cus_123", "pm_stale");
    }

    private Subscription subscriptionWithPaymentMethod(String customerId, String paymentMethodId) {
        User user = new User();
        user.setStripeCustomerId(customerId);
        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUser(user);
        subscription.setStripePaymentMethodId(paymentMethodId);
        return subscription;
    }
}
