package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.application.useCases.GetPlanUseCase;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.stripe.application.useCases.CreateStripeCustomerUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CreateStripeCheckoutUseCase;
import com.jexis.jexis_backend.subscription.application.dto.CreateCheckoutDto;
import com.jexis.jexis_backend.subscription.domain.entities.PendingSubscriptionOperation;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CreateCheckoutUseCaseTest {
    @Test
    void createsAndPersistsReusableCheckout() {
        CreateStripeCheckoutUseCase stripeCheckout = mock(CreateStripeCheckoutUseCase.class);
        GetPlanUseCase getPlan = mock(GetPlanUseCase.class);
        GetAccountUseCase getAccount = mock(GetAccountUseCase.class);
        CreateStripeCustomerUseCase createCustomer = mock(CreateStripeCustomerUseCase.class);
        GetUserUseCase getUser = mock(GetUserUseCase.class);
        UserRepository users = mock(UserRepository.class);
        HandleExistingSubscriptionUseCase handleExisting = mock(HandleExistingSubscriptionUseCase.class);
        SubscriptionOperationCommands commands = mock(SubscriptionOperationCommands.class);
        CreateCheckoutUseCase useCase = new CreateCheckoutUseCase(stripeCheckout, getPlan, getAccount,
                createCustomer, getUser, users, handleExisting, commands);

        UUID userId = UUID.randomUUID();
        Account account = new Account();
        account.setId(UUID.randomUUID());
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setStatus(PlanStatus.PUBLISHED);
        Price price = new Price();
        price.setStripePriceId("price_test");
        plan.setDefaultPrice(price);
        User user = new User();
        user.setStripeCustomerId("cus_test");
        CreateCheckoutDto dto = new CreateCheckoutDto();
        dto.setAccountId(account.getId());
        dto.setPlanId(plan.getId());

        when(getPlan.execute(plan.getId())).thenReturn(plan);
        when(getAccount.execute(account.getId())).thenReturn(account);
        PendingSubscriptionOperation operation = new PendingSubscriptionOperation();
        operation.setId(UUID.randomUUID());
        when(commands.prepare(account.getId(), userId, plan.getId(),
                com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType.CHECKOUT, null))
                .thenReturn(new SubscriptionOperationCommands.Preparation(null, operation));
        when(getUser.execute(userId)).thenReturn(user);
        when(stripeCheckout.execute(plan.getId(), "price_test", userId, account.getId(), "cus_test",
                "subscription-checkout-" + operation.getId()))
                .thenReturn("https://checkout.stripe.com/test");

        var result = useCase.execute(dto, userId);

        assertThat(result.directSubscriptionChange()).isFalse();
        assertThat(result.checkoutUrl()).isEqualTo("https://checkout.stripe.com/test");
        verify(commands).completeCheckout(operation.getId(), "https://checkout.stripe.com/test");
        verifyNoInteractions(handleExisting, createCustomer, users);
    }

    @Test
    void reusesCompletedCheckoutWithoutCallingStripe() {
        CreateStripeCheckoutUseCase stripeCheckout = mock(CreateStripeCheckoutUseCase.class);
        GetPlanUseCase getPlan = mock(GetPlanUseCase.class);
        GetAccountUseCase getAccount = mock(GetAccountUseCase.class);
        SubscriptionOperationCommands commands = mock(SubscriptionOperationCommands.class);
        CreateCheckoutUseCase useCase = new CreateCheckoutUseCase(stripeCheckout, getPlan, getAccount,
                mock(CreateStripeCustomerUseCase.class), mock(GetUserUseCase.class), mock(UserRepository.class),
                mock(HandleExistingSubscriptionUseCase.class), commands);
        UUID userId = UUID.randomUUID();
        Account account = new Account(); account.setId(UUID.randomUUID());
        Plan plan = new Plan(); plan.setId(UUID.randomUUID()); plan.setStatus(PlanStatus.PUBLISHED);
        Price price = new Price(); price.setStripePriceId("price_test"); plan.setDefaultPrice(price);
        CreateCheckoutDto dto = new CreateCheckoutDto();
        dto.setAccountId(account.getId()); dto.setPlanId(plan.getId());
        PendingSubscriptionOperation operation = new PendingSubscriptionOperation();
        operation.setCheckoutUrl("https://checkout.stripe.com/reused");
        when(getPlan.execute(plan.getId())).thenReturn(plan);
        when(getAccount.execute(account.getId())).thenReturn(account);
        when(commands.prepare(account.getId(), userId, plan.getId(),
                com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType.CHECKOUT, null))
                .thenReturn(new SubscriptionOperationCommands.Preparation(null, operation));

        assertThat(useCase.execute(dto, userId).checkoutUrl())
                .isEqualTo("https://checkout.stripe.com/reused");
        verifyNoInteractions(stripeCheckout);
    }
}
