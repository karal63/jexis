package com.jexis.jexis_backend.stripe.application.useCases.plan.price;

import com.jexis.jexis_backend.plan.application.dto.UpdatePriceDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.PriceUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateStripePriceUseCase {
    private final StripeClient client;

    public void execute(String stripePriceId, UpdatePriceDto dto) {
        try {
            PriceUpdateParams.Builder params = PriceUpdateParams.builder();

            if (dto.getActive() != null) {
                params.setActive(dto.getActive());
            }

            client.v1().prices().update(stripePriceId, params.build());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to update Stripe price: " + e.getMessage(), e);
        }
    }
}
