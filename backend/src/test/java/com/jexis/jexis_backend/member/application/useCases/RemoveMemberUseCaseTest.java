package com.jexis.jexis_backend.member.application.useCases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.card.application.useCases.DeleteCardUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.user.domain.entities.User;

class RemoveMemberUseCaseTest {
    private final MemberRepository members = mock(MemberRepository.class);
    private final CardRepository cards = mock(CardRepository.class);
    private final DeleteCardUseCase deleteCard = mock(DeleteCardUseCase.class);
    private final LockAccountUseCase lock = mock(LockAccountUseCase.class);
    private final RemoveMemberUseCase useCase = new RemoveMemberUseCase(members, cards, deleteCard, lock);
    private final UUID memberId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private Card setup() {
        Member member = mock(Member.class);
        Account account = mock(Account.class);
        User user = mock(User.class);
        Card card = mock(Card.class);
        when(account.getId()).thenReturn(accountId);
        when(user.getId()).thenReturn(userId);
        when(member.getAccount()).thenReturn(account);
        when(member.getUser()).thenReturn(user);
        when(members.findById(memberId)).thenReturn(Optional.of(member));
        when(card.getId()).thenReturn(UUID.randomUUID());
        when(cards.findByUserIdAndCardHolderAccountIdAndIsDeletedFalse(userId, accountId))
                .thenReturn(List.of(card));
        return card;
    }

    @Test
    void cancelsAccountCardsBeforeDeletingMembership() {
        Card card = setup();
        useCase.execute(memberId);
        var order = inOrder(lock, cards, deleteCard, members);
        order.verify(lock).execute(accountId);
        order.verify(cards).findByUserIdAndCardHolderAccountIdAndIsDeletedFalse(userId, accountId);
        order.verify(deleteCard).execute(card.getId());
        order.verify(members).deleteById(memberId);
    }

    @Test
    void cancellationFailurePreservesMembership() {
        Card card = setup();
        UUID cardId = card.getId();
        doThrow(new RuntimeException("Stripe unavailable")).when(deleteCard).execute(cardId);
        assertThrows(RuntimeException.class, () -> useCase.execute(memberId));
        verify(members, never()).deleteById(any());
    }
}
