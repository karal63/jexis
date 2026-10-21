package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.SetStripeSubscriptionPaymentMethodUseCase;
import com.jexis.jexis_backend.subscription.application.dto.SetSubscriptionPaymentMethodDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.exceptions.ForbiddenException;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.user.application.useCases.GetPaymentMethodUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SetSubscriptionPaymentMethodUseCase {
    private final SetStripeSubscriptionPaymentMethodUseCase setStripeSubscriptionPaymentMethodUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final GetPaymentMethodUseCase getPaymentMethodUseCase;
    private final GetUserUseCase getUserUseCase;
    private final SubscriptionRepository subscriptionRepository;

    public void execute(UUID subscriptionId, SetSubscriptionPaymentMethodDto dto, UUID userId) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);
        if (subscription.getStripeSubscriptionId() == null) {
            throw new LocalSubscriptionOperationException();
        }
        User user = getUserUseCase.execute(userId);

        PaymentMethod paymentMethod = getPaymentMethodUseCase.execute(user.getStripeCustomerId(), dto.paymentMethodId());
        if (paymentMethod.getCustomer().equals(user.getStripeCustomerId())) {
            setStripeSubscriptionPaymentMethodUseCase.execute(
                    subscription.getStripeSubscriptionId(), dto.paymentMethodId()
            );
            subscription.setStripePaymentMethodId(dto.paymentMethodId());
            subscriptionRepository.save(subscription);
        } else {
            throw new ForbiddenException();
        }

    }
}
