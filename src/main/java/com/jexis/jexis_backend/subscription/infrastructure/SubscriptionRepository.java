package com.jexis.jexis_backend.subscription.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    @Query("""
            select s from Subscription s where s.account.id = :accountId
              and s.currentPeriodStart <= :now and (s.currentPeriodEnd is null or s.currentPeriodEnd > :now)
              and s.status in :statuses
            order by s.currentPeriodStart desc, s.id desc
            """)
    List<Subscription> findEligibleForResourceLimits(
            @Param("accountId") UUID accountId,
            @Param("now") LocalDateTime now,
            @Param("statuses")
            List<SubscriptionStatus> statuses);
    List<Subscription> findByAccountIdAndStripeSubscriptionIdIsNullAndStatus(UUID accountId, SubscriptionStatus status);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Page<Subscription> findByUserId(UUID userId, Pageable pageable);

    @Query("""
                SELECT s
                FROM Subscription s
                WHERE s.account.id = :accountId
                  AND s.currentPeriodStart <= :now
                  AND (s.currentPeriodEnd is null or s.currentPeriodEnd > :now)
                  AND s.status NOT IN (SubscriptionStatus.CANCELED, SubscriptionStatus.INCOMPLETE, SubscriptionStatus.INCOMPLETE_EXPIRED)
            """)
    Optional<Subscription> findActiveSubscription(
            UUID accountId,
            LocalDateTime now
    );
}
