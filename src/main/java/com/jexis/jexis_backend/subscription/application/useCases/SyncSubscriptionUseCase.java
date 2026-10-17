package com.jexis.jexis_backend.subscription.application.useCases;

import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.application.useCases.GetPlanByStripeIdUseCase;
import com.jexis.jexis_backend.plan.application.useCases.GetPlanUseCase;
import com.jexis.jexis_backend.plan.application.useCases.GetPriceByStripeIdUseCase;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.subscription.infrastructure.stripe.SubscriptionMapper;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.StripeClient;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncSubscriptionUseCase {
    private final SubscriptionRepository subscriptionRepository;
    private final GetUserUseCase getUserUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final GetPlanUseCase getPlanUseCase;
    private final GetPriceByStripeIdUseCase getPriceByStripeIdUseCase;
    private final SubscriptionMapper subscriptionMapper;
    private final EntityManager entityManager;
    private final GetPlanByStripeIdUseCase getPlanByStripeIdUseCase;
    private final StripeClient client;

    @Transactional
    public Subscription execute(com.stripe.model.Subscription stripeSub) {
        entityManager.createNativeQuery(
                        "SELECT pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, stripeSub.getId())
                .getSingleResult();

        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSub.getId())
                .orElseGet(Subscription::new);

        String userId = stripeSub.getMetadata().get("userId");
        String accountId = stripeSub.getMetadata().get("accountId");
//        String planId = stripeSub.getMetadata().get("planId");

        User user = getUserUseCase.execute(UUID.fromString(userId));
        Account account = getAccountUseCase.execute(UUID.fromString(accountId));

        var subscriptionItem = stripeSub
                .getItems()
                .getData()
                .get(0);


        Long currentPeriodStart = subscriptionItem.getCurrentPeriodStart();
        Long currentPeriodEnd = subscriptionItem.getCurrentPeriodEnd();

        Price price = getPriceByStripeIdUseCase.execute(subscriptionItem.getPrice().getId());
        Plan plan = getPlanByStripeIdUseCase.execute(subscriptionItem.getPlan().getProduct());

        LocalDateTime convertedPeriodStart = Instant
                .ofEpochSecond(currentPeriodStart)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime convertedPeriodEnd = Instant
                .ofEpochSecond(currentPeriodEnd)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime convertedCanceledAt = stripeSub.getCanceledAt() != null ? Instant
                .ofEpochSecond(stripeSub.getCanceledAt())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime() : null;

        subscription.setUser(user);
        subscription.setAccount(account);
        subscription.setPlan(plan);
        subscription.setPrice(price);
        subscription.setStripeSubscriptionId(stripeSub.getId());
        subscription.setStripePaymentMethodId(stripeSub.getDefaultPaymentMethod());
        subscription.setStatus(subscriptionMapper.mapSubscriptionStatus(stripeSub.getStatus()));
        subscription.setCurrentPeriodStart(convertedPeriodStart);
        subscription.setCurrentPeriodEnd(convertedPeriodEnd);
        subscription.setCancelAtPeriodEnd(stripeSub.getCancelAtPeriodEnd());
        subscription.setCanceledAt(convertedCanceledAt);

        if (stripeSub.getSchedule() != null) {
            subscription.setStripeScheduleId(stripeSub.getSchedule());
            if (subscription.getScheduledPlan() == null && client != null) {
                try {
                    com.stripe.model.SubscriptionSchedule schedule = client.v1().subscriptionSchedules().retrieve(stripeSub.getSchedule());
                    if (schedule.getPhases() != null && schedule.getPhases().size() > 1) {
                        String nextPriceId = schedule.getPhases().get(1).getItems().get(0).getPrice();
                        Price scheduledPrice = getPriceByStripeIdUseCase.execute(nextPriceId);
                        if (scheduledPrice != null) {
                            subscription.setScheduledPlan(scheduledPrice.getPlan());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            subscription.setStripeScheduleId(null);
            subscription.setScheduledPlan(null);
        }

        // Keep free access until Stripe confirms that the paid subscription is usable.
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE
                || subscription.getStatus() == SubscriptionStatus.TRIALING) {
            for (Subscription local : subscriptionRepository.findByAccountIdAndStripeSubscriptionIdIsNullAndStatus(
                    account.getId(), SubscriptionStatus.ACTIVE)) {
                local.setStatus(SubscriptionStatus.CANCELED);
                local.setCanceledAt(LocalDateTime.now(ZoneOffset.UTC));
                local.setCurrentPeriodEnd(local.getCanceledAt());
                subscriptionRepository.save(local);
            }
        }
        return subscriptionRepository.save(subscription);
    }
}
