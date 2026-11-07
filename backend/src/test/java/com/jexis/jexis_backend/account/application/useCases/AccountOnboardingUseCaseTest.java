package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.enums.OnboardingStatus;
import com.jexis.jexis_backend.stripe.application.useCases.CreateLinkUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.RetrieveAccountByIdUseCase;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.jexis.jexis_backend.account.application.useCases.AccountOnboardingUseCase.resolveStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountOnboardingUseCaseTest {
    @Test
    void checksRequirementsAndCapabilitiesBeforeCompletingOnboarding() {
        Account account = new Account();
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.NOT_STARTED);

        account.setDetailsSubmitted(true);
        Account.Requirements requirements = new Account.Requirements();
        account.setRequirements(requirements);
        requirements.setCurrentlyDue(List.of("individual.id_number"));
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.ACTION_REQUIRED);

        requirements.setCurrentlyDue(List.of());
        requirements.setPastDue(List.of("business_profile.url"));
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.ACTION_REQUIRED);

        requirements.setPastDue(List.of());
        Account.Capabilities capabilities = new Account.Capabilities();
        account.setCapabilities(capabilities);
        capabilities.setCardIssuing("active");
        capabilities.setTreasury("pending");
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.PENDING_VERIFICATION);

        capabilities.setTreasury("active");
        requirements.setPendingVerification(List.of("individual.id_number"));
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.PENDING_VERIFICATION);

        requirements.setPendingVerification(List.of());
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.COMPLETED);

        requirements.setDisabledReason("rejected.fraud");
        requirements.setCurrentlyDue(List.of("individual.id_number"));
        assertThat(resolveStatus(account)).isEqualTo(OnboardingStatus.RESTRICTED);
    }

    @Test
    void createsAFreshLinkOnEachAttemptAndStopsOnceStripeStatusChanges() {
        var accounts = mock(GetAccountUseCase.class);
        var stripeAccounts = mock(RetrieveAccountByIdUseCase.class);
        var links = mock(CreateLinkUseCase.class);
        var onboarding = new AccountOnboardingUseCase(accounts, stripeAccounts, links);
        UUID accountId = UUID.randomUUID();
        String connectId = "acct_test";
        when(accounts.execute(accountId)).thenReturn(
                new com.jexis.jexis_backend.account.domain.entities.Account("owner@example.com", connectId, null));
        Account stripeAccount = new Account();
        when(stripeAccounts.execute(connectId)).thenReturn(stripeAccount);
        AccountLink firstLink = new AccountLink();
        firstLink.setUrl("https://connect.stripe.com/setup/first");
        AccountLink secondLink = new AccountLink();
        secondLink.setUrl("https://connect.stripe.com/setup/second");
        var type = AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING;
        when(links.execute(connectId, accountId, type)).thenReturn(firstLink, secondLink);

        assertThat(onboarding.getStatus(accountId)).isEqualTo(OnboardingStatus.NOT_STARTED);
        verifyNoInteractions(links);
        assertThat(onboarding.createLink(accountId).url()).isEqualTo(firstLink.getUrl());
        assertThat(onboarding.createLink(accountId).url()).isEqualTo(secondLink.getUrl());

        stripeAccount.setDetailsSubmitted(true);
        var result = onboarding.createLink(accountId);
        assertThat(result.status()).isEqualTo(OnboardingStatus.PENDING_VERIFICATION);
        assertThat(result.url()).isNull();
        verify(links, times(2)).execute(connectId, accountId, type);
    }
}
