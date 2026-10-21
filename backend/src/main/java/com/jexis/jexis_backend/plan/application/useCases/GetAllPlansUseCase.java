package com.jexis.jexis_backend.plan.application.useCases;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllPlansUseCase {
    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public List<Plan> execute() {
        return planRepository.findAll();
    }
}
