package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelStripeSubscriptionUseCase {
    private final StripeClient client;

    public void execute(String subscriptionId) {
        try {
            SubscriptionUpdateParams params =
                    SubscriptionUpdateParams.builder()
                            .setCancelAtPeriodEnd(true)
                            .build();

            client.v1().subscriptions().update(subscriptionId, params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to cancel subscription", e);
        }
    }
}
