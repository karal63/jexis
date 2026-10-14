package com.jexis.jexis_backend.card.application.security;

import com.jexis.jexis_backend.card.application.useCases.GetCardUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardAuthorizationTest {

    private final HasRoleUseCase hasRoleUseCase = mock(HasRoleUseCase.class);
    private final GetCardUseCase getCardUseCase = mock(GetCardUseCase.class);
    private final GetWalletUseCase getWalletUseCase = mock(GetWalletUseCase.class);
    private final CardAuthorization authorization =
            new CardAuthorization(hasRoleUseCase, getCardUseCase, getWalletUseCase);

    @Test
    void onlyAssignedCardOwnerCanRevealDetails() {
        UUID cardId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Card card = mock(Card.class);
        User owner = mock(User.class);

        when(getCardUseCase.execute(cardId)).thenReturn(card);
        when(card.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);

        assertThat(authorization.canRevealDetails(ownerId, cardId)).isTrue();
        assertThat(authorization.canRevealDetails(UUID.randomUUID(), cardId)).isFalse();
    }
}
