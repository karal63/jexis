package com.jexis.jexis_backend.stripe.application.useCases;

import com.jexis.jexis_backend.wallet.application.dto.CreateOutboundTransferDto;
import com.jexis.jexis_backend.wallet.application.dto.AddReceivedCreditsDto;
import com.jexis.jexis_backend.wallet.domain.entities.ReceivedCreditsNetwork;
import com.stripe.StripeClient;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.OutboundTransferCreateParams;
import com.stripe.param.treasury.ReceivedCreditCreateParams;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WalletStripeCurrencyTest {
    @Test void outboundTransferUsesLowercaseCurrencyAndPreservesAmount() throws Exception {
        var client = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        new StripeOutboundTransferUseCase(client).execute("acct_test", "fa_test", "pm_test",
                new CreateOutboundTransferDto(3_000_000_000L, "USD", "Transfer", UUID.randomUUID()));
        verify(client.v1().treasury().outboundTransfers()).create(
                argThat((OutboundTransferCreateParams p) -> "usd".equals(p.getCurrency())
                        && p.getAmount() == 3_000_000_000L && "fa_test".equals(p.getFinancialAccount())),
                argThat((RequestOptions p) -> "acct_test".equals(p.getStripeAccount())));
    }

    @Test void testFundingUsesLowercaseCurrency() throws Exception {
        var client = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        new AddReceivedCreditsUseCase(client).execute("acct_test", "fa_test",
                new AddReceivedCreditsDto(100L, "USD", ReceivedCreditsNetwork.ach, "Funding"));
        verify(client.v1().testHelpers().treasury().receivedCredits()).create(
                argThat((ReceivedCreditCreateParams p) -> "usd".equals(p.getCurrency()) && p.getAmount() == 100L),
                any(RequestOptions.class));
    }
}
