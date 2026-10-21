package com.jexis.jexis_backend.plan.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;

@Repository
public interface PlanEntitlementRepository extends JpaRepository<PlanEntitlement, UUID> {
    List<PlanEntitlement> findByPlanId(UUID planId);
    Optional<PlanEntitlement> findByPlanIdAndEntitlementId(UUID planId, UUID entitlementId);
}
