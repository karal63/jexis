package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetStripePaymentMethodUseCase {
    private final StripeClient client;

    public PaymentMethod execute(String customerId, String paymentMethodId) {
        try {
            return client.v1().customers().paymentMethods().retrieve(customerId, paymentMethodId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve Stripe payment method", e);
        }
    }
}
