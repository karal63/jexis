package com.jexis.jexis_backend.plan.infrastructure;

import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jexis.jexis_backend.plan.domain.entities.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByCode(String code);

    List<Plan> findAllByStatusAndActiveTrue(PlanStatus status);
    Optional<Plan> findByIdAndStatusAndActiveTrue(UUID id, PlanStatus status);
    Optional<Plan> findByStripePlanId(String stripePlanId);
}
