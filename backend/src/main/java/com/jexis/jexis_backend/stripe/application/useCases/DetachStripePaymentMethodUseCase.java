package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DetachStripePaymentMethodUseCase {
    private final StripeClient client;

    public void execute(String paymentMethodId) {
        try {
            client.v1().paymentMethods().detach(paymentMethodId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to detach payment method", e);
        }
    }
}
