package com.jexis.jexis_backend.card.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;

import com.jexis.jexis_backend.card.application.dto.EditCardDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.stripe.application.useCases.EditCardStatusUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.SetCardLimitsUseCase;

/**
 * EditCardUseCase
 *
 * This service class implements the use case for editing an existing card.
 * It contains only the business logic related to card editing, such as
 * validating input data and interacting with the repository to update the card.
 *
 * Author: Leo
 */
@Service
public class EditCardUseCase {
    private final CardRepository repo;
    private final SetCardLimitsUseCase setCardLimitsUseCase;
    private final EditCardStatusUseCase editCardStatusUseCase;
    private final GetLockedCardUseCase getLockedCard;

    public EditCardUseCase(CardRepository repo,
            SetCardLimitsUseCase setCardLimitsUseCase, EditCardStatusUseCase editCardStatusUseCase,
                           GetLockedCardUseCase getLockedCard) {
        this.getLockedCard = getLockedCard;
        this.repo = repo;
        this.setCardLimitsUseCase = setCardLimitsUseCase;
        this.editCardStatusUseCase = editCardStatusUseCase;
    }

    /**
     * Edits an existing card
     *
     * Accepts a {@link EditCardDto} payload from controller, updates the card,
     * and returns the updated card.
     *
     * @param id the id of the card to be edited and the new card details such as
     *           last4, status, limit, brand, type, currency, and expYear
     * 
     * @return the updated card entity
     */
    @Transactional
    public Card execute(UUID id, EditCardDto dto) {
        Card card = getLockedCard.execute(id);

        if (card.getIsDeleted() || card.isReplacementPending()
                || (card.getStatus() == CardStatus.canceled
                    && dto.status() != null && dto.status() != card.getStatus())) {
            throw new ResourceLimitException(
                    409, "CARD_NOT_EDITABLE", "Canceled, deleted or replacing cards cannot be reactivated or changed");
        }

        if (dto.status() != null) {
            editCardStatusUseCase.execute(card.getCardHolder().getAccount().getConnectAccountId(),
                    card.getStripeCardId(), dto.status());
            card.setStatus(dto.status());
        }

        if (dto.spendingLimits() != null) {
            setCardLimitsUseCase.execute(card.getCardHolder().getAccount().getConnectAccountId(),
                    card.getStripeCardId(), dto.spendingLimits());
            card.setSpendingLimits(dto.spendingLimits());
        }

        Card saved = repo.save(card);

        return saved;
    }
}
