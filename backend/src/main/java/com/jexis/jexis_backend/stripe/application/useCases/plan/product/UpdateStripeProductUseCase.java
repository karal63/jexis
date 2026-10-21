package com.jexis.jexis_backend.stripe.application.useCases.plan.product;

import com.jexis.jexis_backend.plan.application.dto.UpdatePlanDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.ProductUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateStripeProductUseCase {
    private final StripeClient client;

    public void execute(String stripeProductId, UpdatePlanDto dto) {
        try {
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setName(dto.getName())
                    .setDescription(dto.getDescription())
                    .build();
            client.v1().products().update(stripeProductId, params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to update Stripe product: " + e.getMessage(), e);
        }
    }
}
