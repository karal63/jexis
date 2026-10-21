package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.treasury.Transaction;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.FinancialAccountRetrieveParams;
import com.stripe.param.treasury.TransactionRetrieveParams;
import org.springframework.stereotype.Service;

@Service
public class GetStripeTransactionUseCase {
    private final StripeClient client;

    public GetStripeTransactionUseCase(StripeClient client) {
        this.client = client;
    }

    public Transaction execute(String accountId, String transactionId) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();

            return client.v1()
                    .treasury()
                    .transactions()
                    .retrieve(transactionId, options);

        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve transaction", e);
        }
    }
}
