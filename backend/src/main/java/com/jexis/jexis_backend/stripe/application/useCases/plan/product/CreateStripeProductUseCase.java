package com.jexis.jexis_backend.stripe.application.useCases.plan.product;

import com.jexis.jexis_backend.plan.application.dto.CreatePlanDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeProductUseCase {
    private final StripeClient client;

    public Product execute(CreatePlanDto dto) {
        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(dto.getName())
                    .setDescription(dto.getDescription())
                    .build();
            return client.v1().products().create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe product: " + e.getMessage(), e);
        }
    }
}
