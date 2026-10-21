package com.jexis.jexis_backend.stripe.application.useCases.subscription;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionSchedule;
import com.stripe.model.Price;
import com.stripe.param.SubscriptionScheduleCreateParams;
import com.stripe.param.SubscriptionScheduleUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ScheduleStripeSubscriptionDowngradeUseCase {
    private final StripeClient client;

    public SubscriptionSchedule execute(String stripeSubscriptionId, String newStripePriceId) {
        try {
            Subscription subscription = client.v1().subscriptions().retrieve(stripeSubscriptionId);
            Price newPrice = client.v1().prices().retrieve(newStripePriceId);

            SubscriptionSchedule schedule;
            if (subscription.getSchedule() != null) {
                schedule = client.v1().subscriptionSchedules().retrieve(subscription.getSchedule());
            } else {
                SubscriptionScheduleCreateParams createParams = SubscriptionScheduleCreateParams.builder()
                        .setFromSubscription(stripeSubscriptionId)
                        .build();
                schedule = client.v1().subscriptionSchedules().create(createParams);
            }

            SubscriptionSchedule.Phase currentPhase = schedule.getPhases().get(0);

            SubscriptionScheduleUpdateParams.Phase.Builder currentPhaseBuilder = SubscriptionScheduleUpdateParams.Phase.builder()
                    .setStartDate(currentPhase.getStartDate())
                    .setEndDate(currentPhase.getEndDate())
                    .setProrationBehavior(SubscriptionScheduleUpdateParams.Phase.ProrationBehavior.NONE);

            if (currentPhase.getItems() != null) {
                for (SubscriptionSchedule.Phase.Item item : currentPhase.getItems()) {
                    currentPhaseBuilder.addItem(
                            SubscriptionScheduleUpdateParams.Phase.Item.builder()
                                    .setPrice(item.getPrice())
                                    .setQuantity(item.getQuantity())
                                    .build()
                    );
                }
            }

            SubscriptionScheduleUpdateParams.Phase downgradePhase = SubscriptionScheduleUpdateParams.Phase.builder()
                    .setStartDate(currentPhase.getEndDate())
                    .setDuration(
                            SubscriptionScheduleUpdateParams.Phase.Duration.builder()
                                    .setInterval(SubscriptionScheduleUpdateParams.Phase.Duration.Interval.valueOf(
                                            newPrice.getRecurring().getInterval().toUpperCase(Locale.ROOT)
                                    ))
                                    .setIntervalCount(newPrice.getRecurring().getIntervalCount())
                                    .build()
                    )
                    .setProrationBehavior(SubscriptionScheduleUpdateParams.Phase.ProrationBehavior.NONE)
                    .addItem(
                            SubscriptionScheduleUpdateParams.Phase.Item.builder()
                                    .setPrice(newStripePriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            SubscriptionScheduleUpdateParams updateParams = SubscriptionScheduleUpdateParams.builder()
                    .setEndBehavior(SubscriptionScheduleUpdateParams.EndBehavior.RELEASE)
                    .addPhase(currentPhaseBuilder.build())
                    .addPhase(downgradePhase)
                    .build();

            return client.v1().subscriptionSchedules().update(schedule.getId(), updateParams);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to schedule subscription downgrade", e);
        }
    }
}
