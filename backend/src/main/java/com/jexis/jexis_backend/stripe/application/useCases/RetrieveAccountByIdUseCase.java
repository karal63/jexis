package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import org.springframework.stereotype.Service;

@Service
public class RetrieveAccountByIdUseCase {
    private final StripeClient client;

    public RetrieveAccountByIdUseCase(StripeClient client) {
        this.client = client;
    }

    public Account execute(String accountId) {
        try {
            return client.v1().accounts().retrieve(accountId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve account", e);
        }
    }
}
