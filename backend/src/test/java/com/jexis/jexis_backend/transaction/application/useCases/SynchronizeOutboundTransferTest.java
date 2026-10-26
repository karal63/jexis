package com.jexis.jexis_backend.transaction.application.useCases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.*;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletByFAIdUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.stripe.model.treasury.OutboundTransfer;

class SynchronizeOutboundTransferTest {
    private final EntityManager em = mock(EntityManager.class);
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final GetStripeTransactionUseCase stripe = mock(GetStripeTransactionUseCase.class);
    private final GetWalletByFAIdUseCase wallets = mock(GetWalletByFAIdUseCase.class);
    private final SyncBalanceUseCase balances = mock(SyncBalanceUseCase.class);
    private final SynchronizeOutboundTransfer service = new SynchronizeOutboundTransfer(em, repo, stripe, wallets, balances);

    private OutboundTransfer transfer() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq(1), any())).thenReturn(query);
        OutboundTransfer transfer = mock(OutboundTransfer.class, RETURNS_DEEP_STUBS);
        when(transfer.getId()).thenReturn("obt_1");
        when(transfer.getFinancialAccount()).thenReturn("fa_1");
        when(transfer.getTransaction()).thenReturn("tr_1");
        when(transfer.getDestinationPaymentMethodDetails().getUsBankAccount().getNetwork()).thenReturn("ach");
        return transfer;
    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void firstEventCreatesOutgoingTransferWithActualStatus(TransactionStatus status) {
        OutboundTransfer transfer = transfer();
        when(repo.findByStripeObjectId("obt_1")).thenReturn(Optional.empty());
        var treasury = new com.stripe.model.treasury.Transaction();
        treasury.setId("tr_1");
        treasury.setAmount(-2500L);
        treasury.setCurrency("usd");
        when(stripe.execute("acct_1", "tr_1")).thenReturn(treasury);
        Wallet wallet = mock(Wallet.class);
        when(wallets.execute("fa_1")).thenReturn(wallet);

        service.synchronize("acct_1", transfer, status);

        var captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(TransactionType.OUTBOUND_TRANSFER, saved.getType());
        assertEquals(TransactionDirection.DEBIT, saved.getDirection());
        assertEquals(status, saved.getStatus());
        assertEquals(-2500L, saved.getAmount());
        assertEquals("usd", saved.getCurrency());
        assertSame(wallet, saved.getWallet());
        verify(balances).execute("acct_1", "fa_1");
    }

    @ParameterizedTest
    @EnumSource(value = TransactionStatus.class, names = {"COMPLETED", "FAILED", "CANCELED"})
    void terminalUpdateAndLateCreatedEventKeepOneTerminalRecord(TransactionStatus terminal) {
        OutboundTransfer transfer = transfer();
        Transaction existing = new Transaction();
        existing.setStatus(TransactionStatus.PROCESSING);
        when(repo.findByStripeObjectId("obt_1")).thenReturn(Optional.of(existing));

        service.synchronize("acct_1", transfer, terminal);
        assertEquals(terminal, existing.getStatus());
        service.synchronize("acct_1", transfer, TransactionStatus.PROCESSING);
        service.synchronize("acct_1", transfer, terminal);
        assertEquals(terminal, existing.getStatus());
        verify(repo, times(3)).save(existing);
        verify(balances, times(3)).execute("acct_1", "fa_1");
        verifyNoInteractions(stripe, wallets);
    }
}
