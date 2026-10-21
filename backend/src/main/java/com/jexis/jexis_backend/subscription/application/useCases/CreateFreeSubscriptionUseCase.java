package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotActivatedException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFreeSubscriptionUseCase {
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LockAccountUseCase lockAccountUseCase;

    @Transactional
    public Subscription execute(Account account) {
        Account locked = lockAccountUseCase.execute(account.getId());
        if (!Boolean.TRUE.equals(locked.getOwner().getIsActivated())) {
            throw new UserNotActivatedException();
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return subscriptionRepository.findActiveSubscription(locked.getId(), now).orElseGet(() -> {
            var plan = planRepository.findByCode(Plan.FREE_CODE)
                    .orElseThrow(() -> new IllegalStateException("The free plan is not configured"));
            var price = plan.getDefaultPrice();
            if (!plan.isPubliclyAvailable() || price == null || !price.isActive()
                    || !Long.valueOf(0).equals(price.getUnitAmount())) {
                throw new IllegalStateException("The free plan must be published with an active zero-cost price");
            }
            return subscriptionRepository.save(new Subscription(locked.getOwner(), locked, plan, price,
                    null, SubscriptionStatus.ACTIVE, now, null, false));
        });
    }
}
