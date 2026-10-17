package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.plan.application.dto.UpdatePriceDto;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.UpdateStripePriceUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePriceUseCase {
    private final PriceRepository priceRepository;
    private final GetPlanPriceUseCase getPlanPriceUseCase;
    private final UpdateStripePriceUseCase updateStripePriceUseCase;

    @Transactional
    public Price execute(UUID id, UpdatePriceDto dto) {
        boolean changed = false;

        Price price = getPlanPriceUseCase.execute(id);

        if (price.getPlan() != null && price.getPlan().isFreePlan() && Boolean.FALSE.equals(dto.getActive())) {
            throw new ProtectedFreePlanException();
        }

        if (dto.getActive() != null) {
            price.setActive(dto.getActive());
            changed = true;
        }

        if (changed && price.getStripePriceId() != null) {
            updateStripePriceUseCase.execute(price.getStripePriceId(), dto);
        }

        return priceRepository.save(price);
    }
}
