package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.application.useCases.GetPlanUseCase;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanNotPublishedException;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CreateStripeSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.application.dto.CreateSubscriptionDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType;
import com.jexis.jexis_backend.subscription.domain.exceptions.ForbiddenException;
import com.jexis.jexis_backend.user.application.useCases.GetPaymentMethodUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSubscriptionUseCase {
    private final CreateStripeSubscriptionUseCase createStripeSubscriptionUseCase;
    private final GetUserUseCase getUserUseCase;
    private final GetPlanUseCase getPlanUseCase;
    private final GetPaymentMethodUseCase getPaymentMethodUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final HandleExistingSubscriptionUseCase handleExistingSubscriptionUseCase;
    private final SubscriptionOperationCommands subscriptionOperationCommands;

    public void execute(CreateSubscriptionDto dto, UUID userId) {
        User user = getUserUseCase.execute(userId);
        Plan plan = getPlanUseCase.execute(dto.getPlanId());
        Account account = getAccountUseCase.execute(dto.getAccountId());

        if (!plan.isPubliclyAvailable()) {
            throw new PlanNotPublishedException();
        }

        if (plan.isFreePlan() || plan.getDefaultPrice().getStripePriceId() == null) {
            throw new LocalSubscriptionOperationException();
        }
        var preparation = subscriptionOperationCommands.prepare(account.getId(), userId, plan.getId(),
                SubscriptionOperationType.DIRECT, dto.getPaymentMethodId());
        Subscription activeSubscription = preparation.activePaidSubscription();

        // check if account has active subscription with the same plan
        if (activeSubscription != null) {
            handleExistingSubscriptionUseCase.execute(activeSubscription, plan);
        } else {
            var operation = preparation.operation();
            if (operation.getStripeSubscriptionId() != null) {
                return;
            }
            PaymentMethod paymentMethod = getPaymentMethodUseCase.execute(
                    user.getStripeCustomerId(), dto.getPaymentMethodId());
            if (paymentMethod.getCustomer().equals(user.getStripeCustomerId())) {
                com.stripe.model.Subscription stripeSubscription = createStripeSubscriptionUseCase.execute(
                        user.getStripeCustomerId(),
                        dto.getPaymentMethodId(),
                        plan.getDefaultPrice().getStripePriceId(),
                        user.getId(),
                        dto.getAccountId(),
                        plan.getId(),
                        "subscription-create-" + operation.getId()
                );
                subscriptionOperationCommands.completeDirect(operation.getId(), stripeSubscription.getId());
            } else {
                subscriptionOperationCommands.abandon(operation.getId());
                throw new ForbiddenException();
            }
        }
    }
}
