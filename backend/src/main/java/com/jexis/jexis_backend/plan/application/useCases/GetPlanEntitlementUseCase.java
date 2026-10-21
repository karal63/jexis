package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanEntitlementNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlanEntitlementUseCase {
    private final PlanEntitlementRepository planEntitlementRepository;

    @Transactional(readOnly = true)
    public PlanEntitlement execute(UUID planEntitlementId) {
        return planEntitlementRepository.findById(planEntitlementId).orElseThrow(PlanEntitlementNotFoundException::new);
    }
}
