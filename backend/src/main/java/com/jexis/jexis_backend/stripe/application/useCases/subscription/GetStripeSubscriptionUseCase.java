package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetStripeSubscriptionUseCase {
    private final StripeClient client;

    public Subscription execute(String stripeId) {
        try {
            return client.v1().subscriptions().retrieve(stripeId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve subscription from Stripe", e);
        }
    }
}
