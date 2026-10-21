package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateStripeSubscriptionUseCase {
    private final StripeClient client;

    public Subscription execute(
            String customerId, String paymentMethodId, String stripePriceId,
            UUID userId, UUID accountId, UUID planId) {
        try {
            SubscriptionCreateParams params =
                    SubscriptionCreateParams.builder()
                            .setCustomer(customerId)
                            .setDefaultPaymentMethod(paymentMethodId)
                            .addItem(
                                    SubscriptionCreateParams.Item.builder()
                                            .setPrice(stripePriceId)
                                            .setQuantity(1L)
                                            .build()
                            )
                            .putMetadata("userId", userId.toString())
                            .putMetadata("accountId", accountId.toString())
                            .putMetadata("planId", planId.toString())
                            .build();

            Subscription subscription =
                    client.v1().subscriptions().create(params);

            return subscription;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe subscription", e);
        }
    }
}
