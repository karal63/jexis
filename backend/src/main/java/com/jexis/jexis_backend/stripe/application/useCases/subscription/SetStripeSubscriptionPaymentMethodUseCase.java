package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetStripeSubscriptionPaymentMethodUseCase {
    private final StripeClient client;

    public void execute(String subscriptionId, String paymentMethodId) {
        try {
            SubscriptionUpdateParams params =
                    SubscriptionUpdateParams.builder()
                            .setDefaultPaymentMethod(paymentMethodId)
                            .build();

            client.v1().subscriptions().update(
                    subscriptionId,
                    params
            );
        } catch (StripeException e) {
            throw new RuntimeException("Failed to update subscription payment method", e);
        }
    }
}
