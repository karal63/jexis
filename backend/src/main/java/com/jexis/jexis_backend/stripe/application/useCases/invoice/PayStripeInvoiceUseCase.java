package com.jexis.jexis_backend.stripe.application.useCases.invoice;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayStripeInvoiceUseCase {
    private final StripeClient client;

    public void execute(String stripeId) {
        try {
            client.v1().invoices().pay(stripeId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to pay Stripe invoice: " + e.getMessage(), e);
        }
    }
}
