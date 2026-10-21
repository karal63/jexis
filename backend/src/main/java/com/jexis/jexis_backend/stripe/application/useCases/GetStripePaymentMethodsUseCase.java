package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.model.StripeCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetStripePaymentMethodsUseCase {
    private final StripeClient client;

    public StripeCollection<PaymentMethod> execute(String customerId) {
        try {
            return client.v1().customers().paymentMethods().list(customerId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve payment methods", e);
        }
    }
}
