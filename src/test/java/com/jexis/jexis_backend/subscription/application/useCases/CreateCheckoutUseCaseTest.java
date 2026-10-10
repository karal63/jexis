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
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CreateCheckoutUseCaseTest {
    @Test
    void createsCheckoutWhenRepositoryHasNoActiveSubscription() {
        SubscriptionRepository repository = mock(SubscriptionRepository.class);
        CreateStripeCheckoutUseCase stripeCheckout = mock(CreateStripeCheckoutUseCase.class);
        GetPlanUseCase getPlan = mock(GetPlanUseCase.class);
        GetAccountUseCase getAccount = mock(GetAccountUseCase.class);
        CreateStripeCustomerUseCase createCustomer = mock(CreateStripeCustomerUseCase.class);
        GetUserUseCase getUser = mock(GetUserUseCase.class);
        UserRepository users = mock(UserRepository.class);
        HandleExistingSubscriptionUseCase handleExisting = mock(HandleExistingSubscriptionUseCase.class);
        CreateCheckoutUseCase useCase = new CreateCheckoutUseCase(stripeCheckout, getPlan, getAccount,
                createCustomer, getUser, users, new GetActiveAccountSubscriptionUseCase(repository), handleExisting);

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
        when(repository.findActiveSubscription(eq(account.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(getUser.execute(userId)).thenReturn(user);
        when(stripeCheckout.execute(plan.getId(), "price_test", userId, account.getId(), "cus_test"))
                .thenReturn("https://checkout.stripe.com/test");

        var result = useCase.execute(dto, userId);

        assertThat(result.directSubscriptionChange()).isFalse();
        assertThat(result.checkoutUrl()).isEqualTo("https://checkout.stripe.com/test");
        verifyNoInteractions(handleExisting, createCustomer, users);
    }
}
