package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlanPricesUseCase {
    private final PriceRepository priceRepository;

    public List<Price> execute(UUID planId) {
        return priceRepository.findAllByPlanId(planId);
    }
}
