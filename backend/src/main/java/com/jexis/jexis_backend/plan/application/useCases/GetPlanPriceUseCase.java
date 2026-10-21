package com.jexis.jexis_backend.plan.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.exceptions.PriceNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPlanPriceUseCase {
    private final PriceRepository priceRepository;

    public Price execute(UUID id) {
        return priceRepository.findById(id)
                .orElseThrow(PriceNotFoundException::new);
    }
}
