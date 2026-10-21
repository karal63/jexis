package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import com.jexis.jexis_backend.stripe.application.useCases.plan.product.CreateStripeProductUseCase;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.stripe.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.plan.application.dto.CreatePlanDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePlanUseCase {
    private final PlanRepository planRepository;
    private final CreateStripeProductUseCase createStripeProductUseCase;

    @Transactional
    public Plan execute(CreatePlanDto dto) {
        if (Plan.FREE_CODE.equals(dto.getCode())) {
            throw new ProtectedFreePlanException();
        }
        Product stripeProduct = createStripeProductUseCase.execute(dto);
        Plan plan = new Plan(
                stripeProduct.getId(),
                dto.getName(),
                dto.getCode(),
                dto.getDescription(),
                dto.isActive(),
                PlanStatus.DRAFT);
        return planRepository.save(plan);
    }
}
