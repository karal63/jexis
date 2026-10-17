package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import com.jexis.jexis_backend.subscription.domain.exceptions.LocalSubscriptionOperationException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.plan.application.dto.CreatePriceDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.SetStripeDefaultPriceUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.CreateStripePriceUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePriceUseCase {
    private final PriceRepository priceRepository;
    private final PlanRepository planRepository;
    private final GetPlanUseCase getPlanUseCase;
    private final CreateStripePriceUseCase createStripePriceUseCase;
    private final SetStripeDefaultPriceUseCase setStripeDefaultPriceUseCase;

    @Transactional
    public Price execute(UUID planId, CreatePriceDto dto) {
        Plan plan = getPlanUseCase.execute(planId);
        if (plan.isFreePlan()) {
            throw new ProtectedFreePlanException();
        }
        if (plan.getStripePlanId() == null) {
            throw new LocalSubscriptionOperationException();
        }
        com.stripe.model.Price stripePrice = createStripePriceUseCase.execute(plan.getStripePlanId(), dto);

        String interval = normalizeInterval(dto.getInterval());
        Integer intervalCount = dto.getIntervalCount() != null && dto.getIntervalCount() > 0 ? dto.getIntervalCount() : 1;

        Price price = new Price(
                plan,
                stripePrice.getId(),
                dto.getCurrency().toLowerCase(),
                dto.getUnitAmount(),
                interval,
                intervalCount,
                dto.isActive()
        );

        Price savedPrice = priceRepository.save(price);

        if (plan.getDefaultPrice() == null) {
            plan.setDefaultPrice(savedPrice);
            planRepository.save(plan);
            setStripeDefaultPriceUseCase.execute(plan.getStripePlanId(), savedPrice.getStripePriceId());
        }

        return savedPrice;
    }

    private String normalizeInterval(String interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval is required. Supported intervals are monthly and yearly.");
        }
        String lower = interval.trim().toLowerCase();
        return switch (lower) {
            case "month", "monthly" -> "month";
            case "year", "yearly" -> "year";
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval + ". Supported intervals are monthly and yearly.");
        };
    }
}
