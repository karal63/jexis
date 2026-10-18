package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.externalAccount.application.useCases.GetExternalAccountUseCase;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.stripe.application.useCases.*;
import com.jexis.jexis_backend.wallet.application.dto.*;
import com.jexis.jexis_backend.wallet.domain.entities.ReceivedCreditsNetwork;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.domain.exceptions.WalletCurrencyException;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import com.stripe.model.treasury.FinancialAccount;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletCurrencyTest {
    private final Account account = new Account();
    private final Wallet wallet = new Wallet("Expenses", "fa_test", account);
    private final UUID walletId = UUID.randomUUID();
    private final UUID externalId = UUID.randomUUID();
    private final GetWalletUseCase wallets = mock(GetWalletUseCase.class);
    private final GetExternalAccountUseCase externalAccounts = mock(GetExternalAccountUseCase.class);
    private final StripeOutboundTransferUseCase stripeTransfer = mock(StripeOutboundTransferUseCase.class);
    private final CreateOutboundTransferUseCase transfer = new CreateOutboundTransferUseCase(stripeTransfer, wallets, externalAccounts);

    private void destination(String currency) {
        account.setConnectAccountId("acct_test");
        when(wallets.execute(walletId)).thenReturn(wallet);
        ExternalAccount external = new ExternalAccount(account, "pm_test", "Bank", "1234", "US", currency, true);
        when(externalAccounts.execute(externalId)).thenReturn(external);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USD", "usd", "Usd"})
    void matchingTransferAcceptsEitherCase(String currency) {
        destination("usd");
        var body = new CreateOutboundTransferDto(100L, currency, "Transfer", externalId);
        transfer.execute(walletId, body);
        verify(stripeTransfer).execute("acct_test", "fa_test", "pm_test", body);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"EUR", "PLN", " USD", "123"})
    void mismatchedRequestNeverReachesStripe(String currency) {
        destination("usd");
        assertThatThrownBy(() -> transfer.execute(walletId,
                new CreateOutboundTransferDto(100L, currency, "Transfer", externalId)))
                .isInstanceOf(WalletCurrencyException.class);
        verifyNoInteractions(stripeTransfer);
    }

    @Test void mismatchedDestinationNeverReachesStripe() {
        destination("eur");
        assertThatThrownBy(() -> transfer.execute(walletId,
                new CreateOutboundTransferDto(100L, "USD", "Transfer", externalId)))
                .isInstanceOf(WalletCurrencyException.class);
        verifyNoInteractions(stripeTransfer);
    }

    @Test void syncPreservesAmountsLargerThanIntegerRange() {
        var retrieve = mock(RetrieveFinancialAccountUseCase.class);
        var repository = mock(WalletRepository.class);
        var lookup = mock(GetWalletByFAIdUseCase.class);
        var remote = mock(FinancialAccount.class, RETURNS_DEEP_STUBS);
        when(retrieve.execute("acct_test", "fa_test")).thenReturn(remote);
        when(remote.getId()).thenReturn("fa_test");
        when(remote.getBalance().getCash()).thenReturn(Map.of("usd", 3_000_000_000L, "eur", 50L));
        when(lookup.execute("fa_test")).thenReturn(wallet);
        new SyncBalanceUseCase(retrieve, repository, lookup).execute("acct_test", "fa_test");
        assertThat(wallet.getAvailableBalance()).isEqualTo(3_000_000_000L);
        verify(repository).save(wallet);
    }

    @Test void missingCurrencyDoesNotErasePreviousBalance() {
        wallet.setAvailableBalance(500L);
        var retrieve = mock(RetrieveFinancialAccountUseCase.class);
        var repository = mock(WalletRepository.class);
        var lookup = mock(GetWalletByFAIdUseCase.class);
        var remote = mock(FinancialAccount.class, RETURNS_DEEP_STUBS);
        when(retrieve.execute("acct_test", "fa_test")).thenReturn(remote);
        when(remote.getId()).thenReturn("fa_test");
        when(remote.getBalance().getCash()).thenReturn(Map.of("eur", 50L));
        when(lookup.execute("fa_test")).thenReturn(wallet);
        assertThatThrownBy(() -> new SyncBalanceUseCase(retrieve, repository, lookup).execute("acct_test", "fa_test"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("usd");
        assertThat(wallet.getAvailableBalance()).isEqualTo(500L);
        verifyNoInteractions(repository);
    }

    @Test void testFundingRejectsCurrencyMismatch() {
        var stripe = mock(AddReceivedCreditsUseCase.class);
        var accounts = mock(GetAccountUseCase.class);
        when(wallets.execute(walletId)).thenReturn(wallet);
        assertThatThrownBy(() -> new AddMoneyUseCase(stripe, wallets, accounts).execute(UUID.randomUUID(), walletId,
                new AddReceivedCreditsDto(100L, "EUR", ReceivedCreditsNetwork.ach, "Funding")))
                .isInstanceOf(WalletCurrencyException.class);
        verifyNoInteractions(stripe);
    }

    @Test void transferRequestRequiresPositiveAmountAndCurrencyCode() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new CreateOutboundTransferDto(0L, "USD", "Transfer", externalId)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            assertThat(validator.validate(new CreateOutboundTransferDto(-1L, "USD", "Transfer", externalId)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
            assertThat(validator.validate(new CreateOutboundTransferDto(100L, "123", "Transfer", externalId)))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("currency"));
        }
    }
}
