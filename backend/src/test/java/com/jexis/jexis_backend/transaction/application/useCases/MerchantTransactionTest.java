package com.jexis.jexis_backend.transaction.application.useCases;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.authorization.application.useCases.GetAuthorizationByStripeIdUseCase;
import com.jexis.jexis_backend.card.application.useCases.GetCardByStripeIdUseCase;
import com.jexis.jexis_backend.card.presentation.CardWebhookController;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeDebitTransactionUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.*;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.user.application.useCases.GetPaymentMethodUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletByFAIdUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.stripe.Stripe;
import com.stripe.model.treasury.ReceivedDebit;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MerchantTransactionTest {
    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final SyncBalanceUseCase sync = mock(SyncBalanceUseCase.class);
    private final AsyncLogger logger = mock(AsyncLogger.class);
    private final DisputeRepository disputes = mock(DisputeRepository.class);
    private final UpdateCardTransactionUseCase update = new UpdateCardTransactionUseCase(repository, disputes, logger, sync);
    private final DtoHelper mapper = new DtoHelper(mock(GetPaymentMethodUseCase.class));
    private final Account account = new Account();
    private final Wallet wallet = new Wallet("Expenses", "fa_test", account);

    MerchantTransactionTest() {
        account.setConnectAccountId("acct_test");
        account.setOwner(new User());
    }

    @ParameterizedTest
    @CsvSource({"-2000,eur", "-1000,usd", "-3000000000,jpy", "0,eur"})
    void createdWebhookPreservesOriginalUnitsAndSignInStorageAndResponses(long merchantAmount, String merchantCurrency) throws Exception {
        var debitLookup = mock(GetStripeDebitTransactionUseCase.class);
        var treasuryLookup = mock(GetStripeTransactionUseCase.class);
        var walletLookup = mock(GetWalletByFAIdUseCase.class);
        var receivedDebit = new ReceivedDebit();
        receivedDebit.setTransaction("tr_test");
        when(debitLookup.execute("acct_test", "rd_test")).thenReturn(receivedDebit);
        var treasuryTransaction = new com.stripe.model.treasury.Transaction();
        treasuryTransaction.setId("tr_test");
        treasuryTransaction.setFinancialAccount("fa_test");
        when(treasuryLookup.execute("acct_test", "tr_test")).thenReturn(treasuryTransaction);
        when(walletLookup.execute("fa_test")).thenReturn(wallet);
        var create = new CreateCardTransactionUseCase(treasuryLookup, debitLookup, walletLookup, repository,
                mock(GetCardByStripeIdUseCase.class), mock(GetAuthorizationByStripeIdUseCase.class), sync);
        var controller = controller(create);
        String payload = event("created", "\"merchant_amount\":" + merchantAmount + ",\"merchant_currency\":\"" + merchantCurrency + "\",");
        assertThat(controller.handleWebhook(payload, signature(payload)).getStatusCode().value()).isEqualTo(200);
        var captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(captor.capture());
        Transaction stored = captor.getValue();
        assertThat(stored.getAmount()).isEqualTo(-1000L);
        assertThat(stored.getCurrency()).isEqualTo("usd");
        assertMerchantValues(stored, merchantAmount, merchantCurrency);
    }

    @Test void updatedWebhookRefreshesBothAmountsAndCurrencies() throws Exception {
        var stored = existingTransaction();
        when(repository.findByStripeObjectId("ipi_test")).thenReturn(Optional.of(stored));
        String payload = event("updated", "\"merchant_amount\":-2500,\"merchant_currency\":\"pln\",");
        var controller = controller(mock(CreateCardTransactionUseCase.class));
        assertThat(controller.handleWebhook(payload, signature(payload)).getStatusCode().value()).isEqualTo(200);
        verify(repository).save(stored);
        assertMerchantValues(stored, -2500L, "pln");
        assertThat(stored.getAmount()).isEqualTo(-1000L);
        assertThat(stored.getCurrency()).isEqualTo("usd");
    }

    @Test void missingMerchantFieldsDoNotEraseKnownValues() throws Exception {
        var stored = existingTransaction();
        when(repository.findByStripeObjectId("ipi_test")).thenReturn(Optional.of(stored));
        String payload = event("updated", "");
        var controller = controller(mock(CreateCardTransactionUseCase.class));
        controller.handleWebhook(payload, signature(payload));
        verify(repository).save(stored);
        assertMerchantValues(stored, -2000L, "eur");
    }

    @Test void historicalAndBankTransactionsExposeUnknownMerchantValuesWithoutGuessing() {
        var stored = new Transaction(wallet, "tr_bank", "ob_test", TransactionType.OUTBOUND_TRANSFER,
                1000L, "usd", TransactionStatus.COMPLETED, TransactionDirection.DEBIT);
        assertMerchantValues(stored, null, null);
    }

    private Transaction existingTransaction() {
        var stored = new Transaction(wallet, "tr_test", "ipi_test", TransactionType.CARD_TRANSACTION,
                -900L, "usd", TransactionStatus.COMPLETED, TransactionDirection.DEBIT);
        stored.setMerchantAmount(-2000L);
        stored.setMerchantCurrency("eur");
        return stored;
    }

    private void assertMerchantValues(Transaction stored, Long amount, String currency) {
        assertThat(stored.getMerchantAmount()).isEqualTo(amount);
        assertThat(stored.getMerchantCurrency()).isEqualTo(currency);
        var response = mapper.toTransactionDto(stored);
        var admin = mapper.toTransactionAdminDto(stored);
        assertThat(response.merchantAmount()).isEqualTo(amount);
        assertThat(response.merchantCurrency()).isEqualTo(currency);
        assertThat(admin.merchantAmount()).isEqualTo(amount);
        assertThat(admin.merchantCurrency()).isEqualTo(currency);
    }

    private CardWebhookController controller(CreateCardTransactionUseCase create) {
        var controller = new CardWebhookController(logger, create, update);
        ReflectionTestUtils.setField(controller, "webhookSecret", "whsec_test");
        return controller;
    }

    private String event(String action, String merchantFields) {
        return """
                {"id":"evt_test","object":"event","api_version":"%s","account":"acct_test",
                 "type":"issuing_transaction.%s","data":{"object":{
                   "id":"ipi_test","object":"issuing.transaction","amount":-1000,"currency":"usd",
                   %s
                   "card":"ic_test","treasury":{"received_debit":"rd_test"},
                   "merchant_data":{"name":"Shop","category":"general_merchandise","city":"Paris","country":"FR"}
                }}}
                """.formatted(Stripe.API_VERSION, action, merchantFields);
    }

    private String signature(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("whsec_test".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(
                mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
    }
}
