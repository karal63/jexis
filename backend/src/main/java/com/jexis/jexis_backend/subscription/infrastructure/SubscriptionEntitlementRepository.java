package com.jexis.jexis_backend.subscription.infrastructure;

import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionEntitlementRepository extends JpaRepository<SubscriptionEntitlement, UUID> {
    List<SubscriptionEntitlement> findBySubscriptionId(UUID subscriptionId);

    Optional<SubscriptionEntitlement> findBySubscriptionIdAndEntitlementId(UUID subscriptionId, UUID entitlementId);
}
