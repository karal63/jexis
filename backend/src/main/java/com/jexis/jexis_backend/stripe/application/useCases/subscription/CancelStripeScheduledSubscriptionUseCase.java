package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelStripeScheduledSubscriptionUseCase {
    private final StripeClient client;

    public void execute(String stripeSubscriptionId) {
        try {
            Subscription subscription = client.v1().subscriptions().retrieve(stripeSubscriptionId);
            if (subscription.getSchedule() != null) {
                client.v1().subscriptionSchedules().release(subscription.getSchedule());
            }
        } catch (StripeException e) {
            throw new RuntimeException("Failed to cancel scheduled subscription downgrade", e);
        }
    }
}
