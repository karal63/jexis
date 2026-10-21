package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteStripeAccountUseCase {
    private final StripeClient client;

    public DeleteStripeAccountUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId) {
        try {
            client.v1().accounts().delete(accountId);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error deleting account: " + e.getMessage());
        }
    }
}
