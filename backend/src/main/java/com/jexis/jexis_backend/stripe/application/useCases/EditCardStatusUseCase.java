package com.jexis.jexis_backend.stripe.application.useCases;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.stripe.StripeClient;
import com.stripe.model.issuing.Card;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.CardUpdateParams;

@Service
public class EditCardStatusUseCase {
    private final StripeClient client;

    public EditCardStatusUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId, String cardId, CardStatus status) {
        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();
            Card resource = client.v1().issuing().cards().retrieve(cardId, requestOptions);

            CardUpdateParams params = CardUpdateParams.builder().setStatus(getStripeStatus(status)).build();

            resource.update(params, requestOptions);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private CardUpdateParams.Status getStripeStatus(CardStatus status) {
        return switch (status) {
            case active -> CardUpdateParams.Status.ACTIVE;
            case inactive -> CardUpdateParams.Status.INACTIVE;
            case canceled -> CardUpdateParams.Status.CANCELED;
            default -> throw new IllegalArgumentException("Unsupported status: " + status);
        };
    }
}
