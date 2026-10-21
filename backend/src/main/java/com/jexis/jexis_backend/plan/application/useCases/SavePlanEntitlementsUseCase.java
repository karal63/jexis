package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.entitlement.application.useCases.GetEntitlementUseCase;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.plan.application.dto.SavePlanEntitlementDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavePlanEntitlementsUseCase {
    private final PlanEntitlementRepository planEntitlementRepository;
    private final PlanRepository planRepository;
    private final GetEntitlementUseCase getEntitlementUseCase;

    @Transactional
    public List<PlanEntitlement> execute(UUID planId, List<SavePlanEntitlementDto> dtos) {
        Plan plan = planRepository.findById(planId).orElseThrow(PlanNotFoundException::new);

        List<PlanEntitlement> existingList = planEntitlementRepository.findByPlanId(planId);
        Map<UUID, PlanEntitlement> existingMap = existingList.stream()
                .collect(Collectors.toMap(
                        pe -> pe.getEntitlement().getId(),
                        pe -> pe,
                        (a, b) -> a
                ));

        Set<UUID> dtoEntitlementIds = dtos.stream()
                .map(SavePlanEntitlementDto::entitlementId)
                .collect(Collectors.toSet());

        List<PlanEntitlement> toDelete = existingList.stream()
                .filter(pe -> !dtoEntitlementIds.contains(pe.getEntitlement().getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            planEntitlementRepository.deleteAll(toDelete);
        }

        List<PlanEntitlement> toSave = new ArrayList<>();
        for (SavePlanEntitlementDto dto : dtos) {
            PlanEntitlement planEntitlement = existingMap.get(dto.entitlementId());
            if (planEntitlement != null) {
                planEntitlement.setValue(dto.value());
                toSave.add(planEntitlement);
            } else {
                Entitlement entitlement = getEntitlementUseCase.execute(dto.entitlementId());
                PlanEntitlement newPlanEntitlement = new PlanEntitlement(plan, entitlement, dto.value());
                toSave.add(newPlanEntitlement);
            }
        }

        if (toSave.isEmpty()) {
            return List.of();
        }

        return planEntitlementRepository.saveAll(toSave);
    }
}
