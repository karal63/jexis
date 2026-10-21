package com.jexis.jexis_backend.stripe.application.useCases.plan.price;

import com.jexis.jexis_backend.plan.application.dto.CreatePriceDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.param.PriceCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripePriceUseCase {
    private final StripeClient client;

    public Price execute(String stripeProductId, CreatePriceDto dto) {
        try {
            PriceCreateParams.Recurring.Interval interval = mapInterval(dto.getInterval());

            PriceCreateParams.Recurring.Builder recurringBuilder = PriceCreateParams.Recurring.builder()
                    .setInterval(interval);

            if (dto.getIntervalCount() != null && dto.getIntervalCount() > 0) {
                recurringBuilder.setIntervalCount((long) dto.getIntervalCount());
            }

            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(stripeProductId)
                    .setCurrency(dto.getCurrency().toLowerCase())
                    .setUnitAmount(dto.getUnitAmount())
                    .setRecurring(recurringBuilder.build())
                    .setActive(dto.isActive())
                    .build();

            return client.v1().prices().create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe price: " + e.getMessage(), e);
        }
    }

    private PriceCreateParams.Recurring.Interval mapInterval(String interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval is required. Supported intervals are monthly and yearly.");
        }
        String lower = interval.trim().toLowerCase();
        return switch (lower) {
            case "month", "monthly" -> PriceCreateParams.Recurring.Interval.MONTH;
            case "year", "yearly" -> PriceCreateParams.Recurring.Interval.YEAR;
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval + ". Supported intervals are monthly and yearly.");
        };
    }
}
