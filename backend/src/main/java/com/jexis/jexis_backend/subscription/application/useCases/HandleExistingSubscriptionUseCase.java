package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.ScheduleStripeSubscriptionDowngradeUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.UpgradeStripeSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionChangeType;
import com.jexis.jexis_backend.subscription.domain.exceptions.SubscriptionExistsForAccountException;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.stripe.model.SubscriptionSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HandleExistingSubscriptionUseCase {
    private final UpgradeStripeSubscriptionUseCase upgradeStripeSubscriptionUseCase;
    private final ScheduleStripeSubscriptionDowngradeUseCase scheduleStripeSubscriptionDowngradeUseCase;
    private final SubscriptionRepository subscriptionRepository;

    public void execute(Subscription activeSubscription, Plan plan) {
        if (activeSubscription.getPlan().getId().equals(plan.getId())) {
            throw new SubscriptionExistsForAccountException();
        }

        SubscriptionChangeType changeType = activeSubscription.determineChange(plan);

        if (changeType == SubscriptionChangeType.DOWNGRADE) {
            SubscriptionSchedule schedule = scheduleStripeSubscriptionDowngradeUseCase.execute(
                    activeSubscription.getStripeSubscriptionId(),
                    plan.getDefaultPrice().getStripePriceId()
            );
            activeSubscription.setScheduledPlan(plan);
            if (schedule != null) {
                activeSubscription.setStripeScheduleId(schedule.getId());
            }
            subscriptionRepository.save(activeSubscription);
        } else {
            upgradeStripeSubscriptionUseCase.execute(
                    activeSubscription.getStripeSubscriptionId(),
                    plan.getDefaultPrice().getStripePriceId()
            );
            activeSubscription.setScheduledPlan(null);
            activeSubscription.setStripeScheduleId(null);
            subscriptionRepository.save(activeSubscription);
        }
    }
}
