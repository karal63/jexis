package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.DetachStripePaymentMethodUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletePaymentMethodUseCase {
    private final GetUserUseCase getUserUseCase;
    private final GetPaymentMethodUseCase getPaymentMethodUseCase;
    private final DetachStripePaymentMethodUseCase detachStripePaymentMethodUseCase;

    public void execute(UUID userId, String paymentMethodId) {
        User user = getUserUseCase.execute(userId);
        String stripeCustomerId = user.getStripeCustomerId();

        if (stripeCustomerId == null) {
            throw new RuntimeException("Stripe customer is missing for this user");
        }

        getPaymentMethodUseCase.execute(stripeCustomerId, paymentMethodId);
        detachStripePaymentMethodUseCase.execute(paymentMethodId);
    }
}
