package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.treasury.ReceivedDebit;
import com.stripe.net.RequestOptions;
import org.springframework.stereotype.Service;

@Service
public class GetStripeDebitTransactionUseCase {
    private final StripeClient client;

    public GetStripeDebitTransactionUseCase(StripeClient client) {
        this.client = client;
    }

    public ReceivedDebit execute(String accountId, String debitTransactionId) {
        try {
            RequestOptions options = RequestOptions.builder().setStripeAccount(accountId).build();
            return client.v1().treasury().receivedDebits().retrieve(debitTransactionId, options);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve transaction", e);
        }
    }
}
