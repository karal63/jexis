package com.jexis.jexis_backend.account.application.useCases;

import com.stripe.StripeClient;
import com.stripe.model.SetupIntent;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class LinkPayoutBankUseCaseTest {
    @Test
    void usesTheCorrectNameForEachSupportedBusinessType() {
        var account = new com.stripe.model.Account();
        var individual = new com.stripe.model.Person();
        individual.setFirstName("Ada");
        individual.setLastName("Lovelace");
        account.setIndividual(individual);
        account.setBusinessType("individual");
        assertThat(LinkPayoutBankUseCase.accountHolderName(account)).isEqualTo("Ada Lovelace");

        var company = new com.stripe.model.Account.Company();
        company.setName("Example Ltd");
        account.setCompany(company);
        for (String type : new String[]{"company", "non_profit", "government_entity"}) {
            account.setBusinessType(type);
            assertThat(LinkPayoutBankUseCase.accountHolderName(account)).isEqualTo("Example Ltd");
        }
    }

    @Test
    void rejectsAnotherAccountsSetupAndUnconfirmedSetups() throws Exception {
        var accounts = mock(GetAccountUseCase.class);
        var stripe = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        var id = UUID.randomUUID();
        var account = mock(com.jexis.jexis_backend.account.domain.entities.Account.class);
        when(account.getConnectAccountId()).thenReturn("acct_expected");
        when(accounts.execute(id)).thenReturn(account);
        var intent = new SetupIntent();
        intent.setMetadata(Map.of("payout_account", "acct_other"));
        intent.setStatus("succeeded");
        when(stripe.v1().setupIntents().retrieve("seti_test")).thenReturn(intent);
        var useCase = new LinkPayoutBankUseCase(accounts, stripe);

        assertThatThrownBy(() -> useCase.finish(id, "seti_test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        intent.setMetadata(Map.of("payout_account", "acct_expected"));
        intent.setStatus("requires_confirmation");
        assertThatThrownBy(() -> useCase.finish(id, "seti_test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        verify(stripe.v1(), never()).tokens();
    }
}
