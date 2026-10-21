package com.jexis.jexis_backend.stripe.application.useCases;

import com.jexis.jexis_backend.card.domain.enums.CardReplacementReason;
import com.stripe.StripeClient;
import com.stripe.model.StripeCollection;
import com.stripe.model.issuing.Card;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.CardCreateParams;
import com.stripe.param.issuing.CardListParams;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CreateStripeReplacementTest {
    @Test void unsupportedCardCurrencyNeverReachesStripe() {
        StripeClient client = mock(StripeClient.class);
        assertThatThrownBy(() -> new CreateStripeCardUseCase(client).execute("ich_test", "fa_test", "acct_test", "EUR"))
                .isInstanceOf(com.jexis.jexis_backend.wallet.domain.exceptions.WalletCurrencyException.class);
        verifyNoInteractions(client);
    }

    @SuppressWarnings("unchecked")
    @Test void recoversRemoteReplacementInsteadOfIssuingAgain() throws Exception {
        StripeClient client = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        StripeCollection<Card> existing = mock(StripeCollection.class);
        Card recovered = new Card(); recovered.setId("ic_replacement"); recovered.setReplacementFor("ic_original");
        when(existing.autoPagingIterable()).thenReturn(List.of(recovered));
        when(client.v1().issuing().cards().list(any(CardListParams.class), any(RequestOptions.class))).thenReturn(existing);
        Card result = new CreateStripeCardUseCase(client).execute("ich_test", "fa_test", "acct_test", "USD",
                "ic_original", CardReplacementReason.lost, "replace-card-test");
        assertThat(result).isSameAs(recovered);
        verify(client.v1().issuing().cards(), never()).create(any(CardCreateParams.class), any(RequestOptions.class));
    }

    @SuppressWarnings("unchecked")
    @Test void firstIssuanceSendsStableIdempotencyKey() throws Exception {
        StripeClient client = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        StripeCollection<Card> existing = mock(StripeCollection.class);
        when(existing.autoPagingIterable()).thenReturn(List.of());
        when(client.v1().issuing().cards().list(any(CardListParams.class), any(RequestOptions.class))).thenReturn(existing);
        new CreateStripeCardUseCase(client).execute("ich_test", "fa_test", "acct_test", "USD",
                "ic_original", CardReplacementReason.lost, "replace-card-test");
        verify(client.v1().issuing().cards()).create(
                argThat((CardCreateParams params) -> "ic_original".equals(params.getReplacementFor()) && "usd".equals(params.getCurrency())),
                argThat((RequestOptions options) -> "replace-card-test".equals(options.getIdempotencyKey())
                        && "acct_test".equals(options.getStripeAccount())));
    }
}
