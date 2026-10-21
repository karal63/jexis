package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.*;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.card.application.dto.CreateCardDto;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.cardholder.application.useCases.GetCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.member.application.useCases.CanAccessUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.CreateStripeCardUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCardLimitTest {
    @Mock CardRepository cards;
    @Mock CreateStripeCardUseCase stripe;
    @Mock GetCardHolderUseCase holders;
    @Mock GetWalletUseCase wallets;
    @Mock GetAccountUseCase accounts;
    @Mock GetUserUseCase users;
    @Mock CanAccessUseCase access;
    @Mock LockAccountUseCase lock;
    @Mock CheckAccountResourceLimitUseCase limits;
    @InjectMocks CreateCardUseCase useCase;
    CreateCardDto dto;
    CardHolder holder;
    Wallet wallet;

    @BeforeEach void setup() {
        dto = new CreateCardDto();
        dto.setAccountId(UUID.randomUUID()); dto.setUserId(UUID.randomUUID());
        dto.setCardHolderId(UUID.randomUUID()); dto.setWalletId(UUID.randomUUID());
        Account account = new Account(); account.setId(dto.getAccountId());
        holder = new CardHolder(); holder.setAccount(account);
        wallet = new Wallet("test", "fa_test", account);
        when(access.execute(dto.getUserId(), dto.getAccountId())).thenReturn(true);
        when(holders.execute(dto.getCardHolderId())).thenReturn(holder);
        when(wallets.execute(dto.getWalletId())).thenReturn(wallet);
    }
    @Test void deniedCapacityNeverIssuesOrSavesCard() {
        doThrow(new ResourceLimitException(403, "LIMIT_EXCEEDED", "Denied"))
                .when(limits).requireCapacity(dto.getAccountId(), AccountResource.CARDS, 1);
        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(ResourceLimitException.class);
        verify(lock).execute(dto.getAccountId());
        verifyNoInteractions(stripe, cards);
    }
    @Test void crossAccountWalletDeniedBeforeCheckingLimit() {
        Account other = new Account(); other.setId(UUID.randomUUID()); wallet.setAccount(other);
        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(ResourceLimitException.class);
        verifyNoInteractions(limits, stripe, cards);
    }
    @Test void crossAccountHolderDeniedBeforeCheckingLimit() {
        Account other = new Account(); other.setId(UUID.randomUUID()); holder.setAccount(other);
        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(ResourceLimitException.class);
        verifyNoInteractions(limits, stripe, cards);
    }
}
