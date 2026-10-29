package com.jexis.jexis_backend.subscription.application.useCases;

import static org.mockito.Mockito.*;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.application.useCases.GetPlanUseCase;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CreateStripeSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.application.dto.CreateSubscriptionDto;
import com.jexis.jexis_backend.subscription.domain.entities.PendingSubscriptionOperation;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType;
import com.jexis.jexis_backend.user.application.useCases.GetPaymentMethodUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateSubscriptionUseCaseTest {
    @Test
    void createsDirectSubscriptionWithTheReservedOperationId() {
        CreateStripeSubscriptionUseCase stripe = mock(CreateStripeSubscriptionUseCase.class);
        GetUserUseCase getUser = mock(GetUserUseCase.class);
        GetPlanUseCase getPlan = mock(GetPlanUseCase.class);
        GetPaymentMethodUseCase getPaymentMethod = mock(GetPaymentMethodUseCase.class);
        GetAccountUseCase getAccount = mock(GetAccountUseCase.class);
        SubscriptionOperationCommands commands = mock(SubscriptionOperationCommands.class);
        CreateSubscriptionUseCase useCase = new CreateSubscriptionUseCase(stripe, getUser, getPlan,
                getPaymentMethod, getAccount, mock(HandleExistingSubscriptionUseCase.class), commands);
        UUID userId = UUID.randomUUID(), accountId = UUID.randomUUID(), planId = UUID.randomUUID();
        User user = new User(); user.setId(userId); user.setStripeCustomerId("cus_test");
        Account account = new Account(); account.setId(accountId);
        Plan plan = new Plan(); plan.setId(planId); plan.setStatus(PlanStatus.PUBLISHED);
        Price price = new Price(); price.setStripePriceId("price_test"); plan.setDefaultPrice(price);
        PendingSubscriptionOperation operation = new PendingSubscriptionOperation();
        operation.setId(UUID.randomUUID());
        CreateSubscriptionDto dto = new CreateSubscriptionDto();
        dto.setAccountId(accountId); dto.setPlanId(planId); dto.setPaymentMethodId("pm_test");
        PaymentMethod paymentMethod = mock(PaymentMethod.class);
        Subscription subscription = mock(Subscription.class);
        when(getUser.execute(userId)).thenReturn(user);
        when(getPlan.execute(planId)).thenReturn(plan);
        when(getAccount.execute(accountId)).thenReturn(account);
        when(commands.prepare(accountId, userId, planId, SubscriptionOperationType.DIRECT, "pm_test"))
                .thenReturn(new SubscriptionOperationCommands.Preparation(null, operation));
        when(getPaymentMethod.execute("cus_test", "pm_test")).thenReturn(paymentMethod);
        when(paymentMethod.getCustomer()).thenReturn("cus_test");
        when(stripe.execute("cus_test", "pm_test", "price_test", userId, accountId, planId,
                "subscription-create-" + operation.getId())).thenReturn(subscription);
        when(subscription.getId()).thenReturn("sub_test");

        useCase.execute(dto, userId);

        verify(commands).completeDirect(operation.getId(), "sub_test");
    }

    @Test
    void doesNotCallStripeAgainAfterTheDirectOperationHasAProviderResult() {
        CreateStripeSubscriptionUseCase stripe = mock(CreateStripeSubscriptionUseCase.class);
        GetUserUseCase getUser = mock(GetUserUseCase.class);
        GetPlanUseCase getPlan = mock(GetPlanUseCase.class);
        GetAccountUseCase getAccount = mock(GetAccountUseCase.class);
        SubscriptionOperationCommands commands = mock(SubscriptionOperationCommands.class);
        CreateSubscriptionUseCase useCase = new CreateSubscriptionUseCase(stripe, getUser, getPlan,
                mock(GetPaymentMethodUseCase.class), getAccount,
                mock(HandleExistingSubscriptionUseCase.class), commands);
        UUID userId = UUID.randomUUID(), accountId = UUID.randomUUID(), planId = UUID.randomUUID();
        User user = new User(); user.setId(userId); user.setStripeCustomerId("cus_test");
        Account account = new Account(); account.setId(accountId);
        Plan plan = new Plan(); plan.setId(planId); plan.setStatus(PlanStatus.PUBLISHED);
        Price price = new Price(); price.setStripePriceId("price_test"); plan.setDefaultPrice(price);
        PendingSubscriptionOperation operation = new PendingSubscriptionOperation();
        operation.setStripeSubscriptionId("sub_test");
        CreateSubscriptionDto dto = new CreateSubscriptionDto();
        dto.setAccountId(accountId); dto.setPlanId(planId); dto.setPaymentMethodId("pm_test");
        when(getUser.execute(userId)).thenReturn(user);
        when(getPlan.execute(planId)).thenReturn(plan);
        when(getAccount.execute(accountId)).thenReturn(account);
        when(commands.prepare(accountId, userId, planId, SubscriptionOperationType.DIRECT, "pm_test"))
                .thenReturn(new SubscriptionOperationCommands.Preparation(null, operation));

        useCase.execute(dto, userId);

        verifyNoInteractions(stripe);
    }
}
