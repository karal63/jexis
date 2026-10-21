package com.jexis.jexis_backend.transaction.application.security;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionAuthorizationTest {
    UUID userId = UUID.randomUUID(), transactionId = UUID.randomUUID();
    Transaction transaction;
    TransactionAuthorization authorization;
    @BeforeEach void setup() {
        Account account = new Account(); account.setId(UUID.randomUUID());
        Wallet wallet = new Wallet("Test", "fa_test", account);
        User user = new User(); user.setId(userId);
        Card card = new Card(); card.setUser(user);
        transaction = new Transaction(); transaction.setWallet(wallet); transaction.setCard(card);
        GetTransactionUseCase getter = mock(GetTransactionUseCase.class);
        when(getter.execute(transactionId)).thenReturn(transaction);
        authorization = new TransactionAuthorization(mock(HasRoleUseCase.class), getter, mock(GetWalletUseCase.class));
    }
    @ParameterizedTest @EnumSource(value = TransactionType.class, names = {"CARD_TRANSACTION", "CARD_REFUND", "CARD_REVERSAL"})
    void cardOwnerCanReadPaymentsRefundsAndReversals(TransactionType type) {
        transaction.setType(type);
        assertThat(authorization.canView(userId, transactionId)).isTrue();
        assertThat(authorization.canView(UUID.randomUUID(), transactionId)).isFalse();
    }
    @Test void bankTransfersCannotBeReadByOrdinaryUsers() {
        transaction.setType(TransactionType.OUTBOUND_TRANSFER); transaction.setCard(null);
        assertThat(authorization.canView(userId, transactionId)).isFalse();
    }
}
