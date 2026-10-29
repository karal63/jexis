package com.jexis.jexis_backend.subscription.application.useCases;

import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.CancelDuplicateStripeSubscriptionUseCase;
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
import com.jexis.jexis_backend.subscription.infrastructure.PendingSubscriptionOperationRepository;
import com.jexis.jexis_backend.subscription.infrastructure.stripe.SubscriptionMapper;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.StripeClient;
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
    private final GetPlanByStripeIdUseCase getPlanByStripeIdUseCase;
    private final StripeClient client;
    private final LockAccountUseCase lockAccountUseCase;
    private final PendingSubscriptionOperationRepository pendingOperations;
    private final CancelDuplicateStripeSubscriptionUseCase cancelDuplicateStripeSubscriptionUseCase;

    @Transactional
    public Subscription execute(com.stripe.model.Subscription stripeSub) {
        String userId = stripeSub.getMetadata().get("userId");
        String accountId = stripeSub.getMetadata().get("accountId");
        String incomingSubscriptionId = stripeSub.getId();
        UUID parsedAccountId = UUID.fromString(accountId);
        lockAccountUseCase.execute(parsedAccountId);

        var pendingOperation = pendingOperations.findByAccountId(parsedAccountId).orElse(null);
        String canonicalSubscriptionId = pendingOperation == null
                ? null : pendingOperation.getStripeSubscriptionId();
        var otherPaidSubscriptions = subscriptionRepository
                .findNonTerminalPaidSubscriptions(parsedAccountId)
                .stream()
                .filter(existing -> !incomingSubscriptionId.equals(existing.getStripeSubscriptionId()))
                .toList();
        if (canonicalSubscriptionId == null && !otherPaidSubscriptions.isEmpty()) {
            canonicalSubscriptionId = otherPaidSubscriptions.get(0).getStripeSubscriptionId();
        }
        if (canonicalSubscriptionId == null) {
            canonicalSubscriptionId = incomingSubscriptionId;
            if (pendingOperation != null) {
                pendingOperation.setStripeSubscriptionId(canonicalSubscriptionId);
            }
        }

        boolean duplicate = !incomingSubscriptionId.equals(canonicalSubscriptionId);
        if (duplicate) {
            stripeSub = cancelDuplicateStripeSubscriptionUseCase.execute(stripeSub.getId());
        } else {
            LocalDateTime canceledAt = LocalDateTime.now(ZoneOffset.UTC);
            for (Subscription other : otherPaidSubscriptions) {
                cancelDuplicateStripeSubscriptionUseCase.execute(other.getStripeSubscriptionId());
                other.setStatus(SubscriptionStatus.CANCELED);
                other.setCancelAtPeriodEnd(false);
                other.setCanceledAt(canceledAt);
                other.setCurrentPeriodEnd(canceledAt);
                subscriptionRepository.save(other);
            }
        }

        Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSub.getId())
                .orElseGet(Subscription::new);
//        String planId = stripeSub.getMetadata().get("planId");

        User user = getUserUseCase.execute(UUID.fromString(userId));
        Account account = getAccountUseCase.execute(parsedAccountId);

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
            if (!duplicate && pendingOperation != null) {
                pendingOperations.delete(pendingOperation);
            }
        }
        return subscriptionRepository.save(subscription);
    }
}
