package com.jexis.jexis_backend.stripe.application.useCases;

import com.jexis.jexis_backend.card.application.dto.CreateEphemeralKeyDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.EphemeralKey;
import com.stripe.net.RequestOptions;
import com.stripe.param.EphemeralKeyCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetStripeEphemeralKeyUseCase {
    private final StripeClient client;
    private static final String STRIPE_ISSUING_ELEMENTS_API_VERSION = "2026-04-22.dahlia";

    public String execute(String stripeCardId, String connectAccountId, CreateEphemeralKeyDto body) {
        try {
            EphemeralKeyCreateParams params =
                    EphemeralKeyCreateParams.builder()
                            .setIssuingCard(stripeCardId)
                            .setNonce(body.nonce())
                            .setStripeVersion(STRIPE_ISSUING_ELEMENTS_API_VERSION)
                            .build();

            RequestOptions options =
                    RequestOptions.builder()
                            .setStripeAccount(
                                    connectAccountId
                            )
                            .build();

            EphemeralKey ephemeralKey =
                    client.v1()
                            .ephemeralKeys()
                            .create(params, options);

            return ephemeralKey.getSecret();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create ephemeral key: " + e.getMessage(), e);
        }
    }
}
