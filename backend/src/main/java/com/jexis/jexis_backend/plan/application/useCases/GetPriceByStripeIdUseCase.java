package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.exceptions.PriceNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPriceByStripeIdUseCase {
    private final PriceRepository priceRepository;

    public Price execute(String stripeId) {
        return priceRepository.findByStripePriceId(stripeId)
                .orElseThrow(PriceNotFoundException::new);
    }
}
