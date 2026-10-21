package com.jexis.jexis_backend.stripe.application.useCases.plan.price;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.PriceUpdateParams;
import com.stripe.param.ProductUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetStripeDefaultPriceUseCase {
    private final StripeClient client;

    public void execute(String productId, String priceId) {
        try {
            ProductUpdateParams params = ProductUpdateParams.builder()
                    .setDefaultPrice(priceId).build();

            client.v1().products().update(productId, params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to set default price: " + e.getMessage(), e);
        }

    }
}
