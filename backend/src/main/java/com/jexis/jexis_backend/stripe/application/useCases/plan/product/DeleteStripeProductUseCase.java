package com.jexis.jexis_backend.stripe.application.useCases.plan.product;

import com.stripe.StripeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteStripeProductUseCase {
    private final StripeClient client;

    public void execute(String stripeProductId) {
        try {
            client.v1().products().delete(stripeProductId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete Stripe product: " + e.getMessage(), e);
        }
    }
}
