package com.jexis.jexis_backend.stripe.application.useCases;

import com.jexis.jexis_backend.wallet.application.dto.AddReceivedCreditsDto;
import com.jexis.jexis_backend.wallet.domain.entities.ReceivedCreditsNetwork;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.treasury.ReceivedCredit;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.ReceivedCreditCreateParams;
import org.springframework.stereotype.Service;
import com.jexis.jexis_backend.wallet.domain.WalletCurrency;

@Service
public class AddReceivedCreditsUseCase {
    private final StripeClient client;

    public AddReceivedCreditsUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId, String financialAccountId, AddReceivedCreditsDto body) {
        try {
            ReceivedCreditCreateParams params =
                    ReceivedCreditCreateParams.builder()
                            .setFinancialAccount(financialAccountId)
                            .setNetwork(getNetwork(body.network()))
                            .setCurrency(WalletCurrency.toStripe(body.currency()))
                            .setAmount(body.amount())
                            .build();

            RequestOptions requestOptions =
                    RequestOptions.builder().setStripeAccount(accountId).build();

            ReceivedCredit receivedCredit = client.v1().testHelpers().treasury().receivedCredits().create(params, requestOptions);
        } catch(StripeException e) {
            throw new RuntimeException("Error creating received Credit", e);
        }
    }

    private ReceivedCreditCreateParams.Network  getNetwork(ReceivedCreditsNetwork network) {
        return switch(network) {
            case ach -> ReceivedCreditCreateParams.Network.ACH;
            case us_domestic_wire -> ReceivedCreditCreateParams.Network.US_DOMESTIC_WIRE;
            default -> throw new IllegalArgumentException("Unsupported received credit network: " + network);
        };
    }
}
