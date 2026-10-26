package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelDuplicateStripeSubscriptionUseCase {
    private final StripeClient client;

    public Subscription execute(String subscriptionId) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("cancel-duplicate-subscription-" + subscriptionId)
                    .build();
            return client.v1().subscriptions().cancel(subscriptionId, options);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to cancel duplicate Stripe subscription", e);
        }
    }
}
