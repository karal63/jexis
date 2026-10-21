package com.jexis.jexis_backend.plan.application.useCases;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPlanEntitlementsUseCase {
    private final PlanEntitlementRepository planEntitlementRepository;

    @Transactional(readOnly = true)
    public List<PlanEntitlement> execute(UUID planId) {
        return planEntitlementRepository.findByPlanId(planId);
    }
}
