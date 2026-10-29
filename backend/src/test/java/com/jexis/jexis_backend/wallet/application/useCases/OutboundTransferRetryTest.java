package com.jexis.jexis_backend.wallet.application.useCases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.externalAccount.application.useCases.GetExternalAccountUseCase;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.stripe.application.useCases.StripeOutboundTransferUseCase;
import com.jexis.jexis_backend.wallet.application.dto.CreateOutboundTransferDto;
import com.jexis.jexis_backend.wallet.domain.entities.*;
import com.jexis.jexis_backend.wallet.infrastructure.OutboundTransferCommandRepository;

class OutboundTransferRetryTest {
    final OutboundTransferCommandRepository repo = mock(OutboundTransferCommandRepository.class);
    final OutboundTransferCommands commands = new OutboundTransferCommands(repo, mock(LockAccountUseCase.class));
    final GetWalletUseCase wallets = mock(GetWalletUseCase.class);
    final GetExternalAccountUseCase destinations = mock(GetExternalAccountUseCase.class);
    final StripeOutboundTransferUseCase stripe = mock(StripeOutboundTransferUseCase.class);
    final CreateOutboundTransferUseCase service = new CreateOutboundTransferUseCase(stripe, wallets, destinations, commands);
    final UUID walletId = UUID.randomUUID(), destinationId = UUID.randomUUID(), key = UUID.randomUUID();
    final CreateOutboundTransferDto body = new CreateOutboundTransferDto(500L, "usd", "Test", destinationId);
    final Wallet wallet;
    final ExternalAccount destination;
    OutboundTransferCommand saved;

    OutboundTransferRetryTest() {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setConnectAccountId("acct_1");
        wallet = new Wallet("Test", "fa_1", account);
        wallet.setId(walletId);
        destination = new ExternalAccount(account, "pm_1", "Bank", "1234", "US", "usd", true);
        when(wallets.execute(walletId)).thenReturn(wallet);
        when(destinations.execute(destinationId)).thenReturn(destination);
        when(repo.findByWalletIdAndRequestKey(walletId, key)).thenAnswer(i -> Optional.ofNullable(saved));
        when(repo.save(any())).thenAnswer(i -> { saved = i.getArgument(0); return saved; });
        when(repo.findById(any())).thenAnswer(i -> Optional.of(saved));
    }

    @Test void lostResponseRetriesSameCommandAndCompletedRequestNeverSendsAgain() {
        when(stripe.execute(anyString(), anyString(), anyString(), any(), anyString()))
                .thenThrow(new RuntimeException("Response lost")).thenReturn("obt_1");
        assertThrows(RuntimeException.class, () -> service.execute(walletId, body, key));
        UUID commandId = saved.getId();
        service.execute(walletId, body, key);
        service.execute(walletId, body, key);
        verify(stripe, times(2)).execute("acct_1", "fa_1", "pm_1", body, "outbound-transfer-" + commandId);
        verify(repo, times(1)).save(any());
        assertEquals("obt_1", saved.getStripeTransferId());
    }

    @Test void changedPayloadRejectedBeforeStripe() {
        commands.prepare(wallet, destination, body, key);
        var changed = new CreateOutboundTransferDto(600L, "usd", "Test", destinationId);
        assertThrows(ResourceLimitException.class, () -> service.execute(walletId, changed, key));
        verifyNoInteractions(stripe);
    }

    @Test void oldUnknownOutcomeRequiresReconciliation() {
        commands.prepare(wallet, destination, body, key);
        saved.setCreatedAt(Instant.now().minus(Duration.ofHours(24)));
        var error = assertThrows(ResourceLimitException.class, () -> service.execute(walletId, body, key));
        assertEquals("TRANSFER_RECONCILIATION_REQUIRED", error.getCode());
        verifyNoInteractions(stripe);
    }

    @Test void oldCompletedRequestStillReturnsWithoutSending() {
        commands.prepare(wallet, destination, body, key);
        saved.setCreatedAt(Instant.now().minus(Duration.ofDays(10)));
        saved.setStripeTransferId("obt_1");
        service.execute(walletId, body, key);
        verifyNoInteractions(stripe);
    }
}
