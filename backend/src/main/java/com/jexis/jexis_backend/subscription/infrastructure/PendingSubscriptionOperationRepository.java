package com.jexis.jexis_backend.subscription.infrastructure;

import com.jexis.jexis_backend.subscription.domain.entities.PendingSubscriptionOperation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingSubscriptionOperationRepository
        extends JpaRepository<PendingSubscriptionOperation, UUID> {
    Optional<PendingSubscriptionOperation> findByAccountId(UUID accountId);
}
