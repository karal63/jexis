package com.jexis.jexis_backend.card.application.useCases;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.stripe.application.useCases.EditCardStatusUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;

/**
 * DeleteCardUseCase
 * <p>
 * This service class implements the use case for deleting an existing card.
 * It contains only the business logic related to card deletion, such as
 * validating the user's permission and interacting with the repository to
 * remove the card.
 * <p>
 * Author: Leo
 */
@Service
public class DeleteCardUseCase {
    private final CardRepository repo;
    private final EditCardStatusUseCase editCardStatusUseCase;
    private final GetLockedCardUseCase getLockedCard;

    public DeleteCardUseCase(CardRepository repo, EditCardStatusUseCase editCardStatusUseCase,
            GetLockedCardUseCase getLockedCard) {
        this.getLockedCard = getLockedCard;
        this.repo = repo;
        this.editCardStatusUseCase = editCardStatusUseCase;
    }

    /**
     * Deletes an existing card
     * <p>
     * Accepts a {@param cardId} and removes the card from the repository.
     *
     * @param cardId id of the card we want to delete
     */
    @Transactional
    public void execute(UUID cardId) {
        Card card = getLockedCard.execute(cardId);
        if (card.isReplacementPending()) {
            throw new ResourceLimitException(
                    409, "CARD_REPLACEMENT_PENDING", "Complete the pending replacement before deleting this card");
        }

        editCardStatusUseCase.execute(
                card.getCardHolder().getAccount().getConnectAccountId(),
                card.getStripeCardId(),
                CardStatus.canceled
        );

        card.setIsDeleted(true);
        card.setStatus(CardStatus.canceled);
        card.setDeletedAt(LocalDateTime.now());

        repo.save(card);
    }
}
