package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.FinancialAccountCloseParams;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CloseStripeWalletUseCase {
    private final StripeClient client;

    public CloseStripeWalletUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId, String financialAccountId) {
        try {
            RequestOptions options = RequestOptions.builder().setStripeAccount(accountId).build();
            client.v1().treasury().financialAccounts().close(financialAccountId, options);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error closing financial treasury account: " + e.getMessage());
        }
    }
}
