package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPublicPlansUseCase {
    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<Plan> execute() {
        return planRepository.findAllByStatusAndActiveTrue(PlanStatus.PUBLISHED);
    }
}
