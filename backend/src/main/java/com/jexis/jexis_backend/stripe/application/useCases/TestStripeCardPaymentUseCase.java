package com.jexis.jexis_backend.stripe.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.issuing.Transaction;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.TransactionCreateForceCaptureParams;
import org.springframework.stereotype.Service;

@Service
public class TestStripeCardPaymentUseCase {
    private final StripeClient client;

    public TestStripeCardPaymentUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String cardId, Long amount, String currency, String accountId) {
        try {
            TransactionCreateForceCaptureParams params =
                    TransactionCreateForceCaptureParams.builder()
                            .setAmount(amount)
                            .setCard(cardId)
                            .setCurrency(currency)
                            .setMerchantData(
                                    TransactionCreateForceCaptureParams.MerchantData.builder()
                                            .setCountry("US")
                                            .setNetworkId("123456789")
                                            .setTerminalId("123456789")
                                            .setCategory(
                                                    TransactionCreateForceCaptureParams.MerchantData.Category.TRANSPORTATION_SERVICES
                                            )
                                            .setName("Rocket Rides")
                                            .build()
                            )
                            .build();

            RequestOptions requestOptions =
                    RequestOptions.builder().setStripeAccount(accountId).build();

            client.v1().testHelpers().issuing().transactions().createForceCapture(params, requestOptions);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe exception during creating a new test card payment: " + e);
        }
    }
}
