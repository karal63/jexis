package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.card.application.dto.ReplaceCardDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.stripe.application.useCases.CreateStripeCardUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.EditCardStatusUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardReplacementTransactions {
    private final GetLockedCardUseCase getLockedCard;
    private final CheckAccountResourceLimitUseCase checkLimit;
    private final CardRepository repository;
    private final EditCardStatusUseCase editStripeCard;
    private final CreateStripeCardUseCase createStripeCard;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void prepare(UUID cardId, ReplaceCardDto dto) {
        Card card = getLockedCard.execute(cardId);
        if (card.getReplacementCard() != null) return;
        checkLimit.requireEligibleSubscription(card.getCardHolder().getAccount().getId());
        if (dto == null || dto.replacementReason() == null) {
            throw new ResourceLimitException(400, "INVALID_REPLACEMENT_REASON", "A replacement reason is required");
        }
        if (card.isReplacementPending()) {
            if (card.getReplacementReason() != dto.replacementReason()) {
                throw new ResourceLimitException(409, "REPLACEMENT_REASON_MISMATCH", "Retry with the original replacement reason");
            }
            return;
        }
        if (card.getIsDeleted() || card.getStatus() == CardStatus.canceled) {
            throw new ResourceLimitException(409, "CARD_NOT_REPLACEABLE", "Only a counted card can be replaced");
        }
        editStripeCard.execute(card.getCardHolder().getAccount().getConnectAccountId(),
                card.getStripeCardId(), CardStatus.canceled);
        card.setStatus(CardStatus.canceled);
        card.setReplacementPending(true);
        card.setReplacementReason(dto.replacementReason());
        repository.saveAndFlush(card);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Card issue(UUID cardId) {
        Card card = getLockedCard.execute(cardId);
        if (card.getReplacementCard() != null) return card.getReplacementCard();
        checkLimit.requireEligibleSubscription(card.getCardHolder().getAccount().getId());
        if (!card.isReplacementPending()) {
            throw new ResourceLimitException(409, "NO_PENDING_REPLACEMENT", "Prepare the replacement first");
        }
        var stripeCard = createStripeCard.execute(card.getCardHolder().getStripeCardHolderId(),
                card.getTreasuryAccount().getStripeFinancialAccountId(),
                card.getCardHolder().getAccount().getConnectAccountId(),
                card.getTreasuryAccount().getCurrency(),
                card.getStripeCardId(), card.getReplacementReason(), "replace-card-" + cardId);
        Card replacement = new Card(stripeCard.getId(), card.getCardHolder(), card.getTreasuryAccount(),
                card.getUser(), stripeCard.getLast4(), CardStatus.valueOf(stripeCard.getStatus()),
                stripeCard.getBrand(), stripeCard.getType(), stripeCard.getCurrency(), stripeCard.getExpYear());
        replacement.setSpendingLimits(card.getSpendingLimits());
        repository.saveAndFlush(replacement);
        card.setReplacementCard(replacement);
        card.setReplacementPending(false);
        repository.saveAndFlush(card);
        return replacement;
    }
}
