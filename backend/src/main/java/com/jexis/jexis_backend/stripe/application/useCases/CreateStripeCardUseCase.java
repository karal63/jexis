package com.jexis.jexis_backend.stripe.application.useCases;

import org.springframework.stereotype.Service;
import com.jexis.jexis_backend.wallet.domain.WalletCurrency;

import com.stripe.StripeClient;
import com.stripe.model.issuing.Card;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.CardCreateParams;
import com.stripe.param.issuing.CardListParams;
import com.jexis.jexis_backend.card.domain.enums.CardReplacementReason;

@Service
public class CreateStripeCardUseCase {
    private final StripeClient client;

    public CreateStripeCardUseCase(StripeClient client) {
        this.client = client;
    }

    public Card execute(String cardholderId, String financialAccountId, String connectedAccountId, String currency) {
        return execute(cardholderId, financialAccountId, connectedAccountId, currency, null, null);
    }

    public Card execute(String cardholderId, String financialAccountId, String connectedAccountId, String currency, String replacementFor,
            CardReplacementReason replacementReason) {
        return execute(cardholderId, financialAccountId, connectedAccountId, currency, replacementFor, replacementReason, null);
    }

    public Card execute(String cardholderId, String financialAccountId, String connectedAccountId, String currency, String replacementFor,
            CardReplacementReason replacementReason, String idempotencyKey) {
        String stripeCurrency = WalletCurrency.toStripe(currency);
        try {
            // Recover a prior successful issuance even if its local commit failed.
            // This also avoids relying solely on Stripe's finite idempotency retention.
            if (replacementFor != null && idempotencyKey != null) {
                var existing = client.v1().issuing().cards().list(
                        CardListParams.builder().setCardholder(cardholderId).build(),
                        RequestOptions.builder().setStripeAccount(connectedAccountId).build());
                Card recovered = null;
                for (Card candidate : existing.autoPagingIterable()) {
                    if (replacementFor.equals(candidate.getReplacementFor())) {
                        if (recovered != null) throw new IllegalStateException("Multiple Stripe replacements require reconciliation");
                        recovered = candidate;
                    }
                }
                if (recovered != null) return recovered;
            }
            CardCreateParams.Builder builder = CardCreateParams.builder()
                    .setCardholder(cardholderId)
                    .setFinancialAccount(financialAccountId)
                    .setCurrency(stripeCurrency)
                    .setType(CardCreateParams.Type.VIRTUAL)
                    .setStatus(CardCreateParams.Status.ACTIVE);

            if (replacementFor != null) {
                builder.setReplacementFor(replacementFor);
            }

            if (replacementReason != null) {
                builder.setReplacementReason(mapReplacementReason(replacementReason));
            }

            CardCreateParams params = builder.build();

            RequestOptions.RequestOptionsBuilder options = RequestOptions.builder().setStripeAccount(connectedAccountId);
            if (idempotencyKey != null) options.setIdempotencyKey(idempotencyKey);
            RequestOptions requestOptions = options.build();

            Card card = client.v1().issuing().cards().create(params, requestOptions);
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private CardCreateParams.ReplacementReason mapReplacementReason(CardReplacementReason replacementReason) {
        return switch (replacementReason) {
            case damaged -> CardCreateParams.ReplacementReason.DAMAGED;
            case expired -> CardCreateParams.ReplacementReason.EXPIRED;
            case lost -> CardCreateParams.ReplacementReason.LOST;
            case stolen -> CardCreateParams.ReplacementReason.STOLEN;
        };
    }
}
