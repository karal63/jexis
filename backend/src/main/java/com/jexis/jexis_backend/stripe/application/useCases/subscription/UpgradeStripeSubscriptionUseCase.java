package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpgradeStripeSubscriptionUseCase {
    private final StripeClient client;

    public void execute(String stripeSubscriptionId, String newStripePriceId) {
        try {
            Subscription subscription = client.v1().subscriptions().retrieve(stripeSubscriptionId);

            if (subscription.getSchedule() != null) {
                client.v1().subscriptionSchedules().release(subscription.getSchedule());
            }

            SubscriptionItem subscriptionItem = client.v1().subscriptionItems().retrieve(subscription.getItems().getData().get(0).getId());

            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.ALWAYS_INVOICE)
                    .addItem(
                            SubscriptionUpdateParams.Item.builder()
                                    .setId(subscriptionItem.getId())
                                    .setPrice(newStripePriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            client.v1().subscriptions().update(
                    stripeSubscriptionId,
                    params
            );
        } catch (StripeException e) {
            throw new RuntimeException("Failed to upgrade subscription", e);
        }
    }
}
