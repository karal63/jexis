package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.treasury.OutboundTransfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.OutboundTransferCancelParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeCancelOutboundTransferUseCase {
    private final StripeClient client;

    public void execute(String accountId, String transferId) {
        try {
            OutboundTransfer resource = client.v1().treasury().outboundTransfers().retrieve(transferId);

            OutboundTransferCancelParams params = OutboundTransferCancelParams.builder().build();

            RequestOptions requestOptions =
                    RequestOptions.builder().setStripeAccount(accountId).build();

            resource.cancel(params, requestOptions);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error canceling outbound transfer", e);
        }
    }
}
