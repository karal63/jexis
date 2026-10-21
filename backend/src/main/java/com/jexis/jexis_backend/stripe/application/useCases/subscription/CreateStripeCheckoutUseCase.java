package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateStripeCheckoutUseCase {
    @Value("${application.origin}")
    private String origin;
    private final StripeClient client;

    public String execute(UUID planId, String stripePriceId, UUID userId, UUID accountId, String stripeAccountId) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(stripePriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(stripeAccountId)
                    .setClientReferenceId(userId.toString())
                    .setSubscriptionData(
                            SessionCreateParams.SubscriptionData.builder()
                                    .putMetadata("userId", userId.toString())
                                    .putMetadata("accountId", accountId.toString())
                                    .putMetadata("planId", planId.toString())
                                    .build()
                    )
                    .setSuccessUrl(origin + "/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(origin + "/cancel")
                    .build();

            Session session = client.v1().checkout().sessions().create(params);

            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe checkout session", e);
        }
    }
}
