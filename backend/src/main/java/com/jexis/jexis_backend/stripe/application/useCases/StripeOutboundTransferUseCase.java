package com.jexis.jexis_backend.stripe.application.useCases;

import com.jexis.jexis_backend.wallet.application.dto.CreateOutboundTransferDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.OutboundTransferCreateParams;
import org.springframework.stereotype.Service;
import com.jexis.jexis_backend.wallet.domain.WalletCurrency;

@Service
public class StripeOutboundTransferUseCase {
    private final StripeClient client;

    public StripeOutboundTransferUseCase(StripeClient client) {
        this.client = client;
    }

    public String execute(String accountId, String financialAccountId, String externalAccountId, CreateOutboundTransferDto dto, String idempotencyKey) {
        try {
            OutboundTransferCreateParams params =
                    OutboundTransferCreateParams.builder()
                            .setAmount(dto.amount())
                            .setCurrency(WalletCurrency.toStripe(dto.currency()))
                            .setFinancialAccount(financialAccountId)
                            .setDescription(dto.description())
                            .setDestinationPaymentMethod(externalAccountId)
                            .build();

            RequestOptions requestOptions =
                    RequestOptions.builder().setStripeAccount(accountId).setIdempotencyKey(java.util.Objects.requireNonNull(idempotencyKey)).build();

            return client.v1().treasury().outboundTransfers().create(params, requestOptions).getId();

        } catch(StripeException e) {
            throw new RuntimeException("Error during creating outbound transfer", e);
        }
    }
}
