package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.model.treasury.FinancialAccount;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.FinancialAccountRetrieveParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrieveFinancialAccountUseCase {
    private final StripeClient client;

    public FinancialAccount execute(String accountId, String financialAccountId) {
        try {
            RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(accountId).build();
            return client.v1().treasury().financialAccounts().retrieve(financialAccountId, requestOptions);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving financial account", e);
        }
    }
}
