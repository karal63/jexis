package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.exceptions.CardNotFoundException;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * GetCardUseCase
 * This service class implements the use case for retrieving a single card.
 * It contains only the business logic related to fetching a card, such as
 * interacting with the repository to fetch the requested card.
 * Author: Leo
 */
@Service
public class GetCardByStripeIdUseCase {
    private final CardRepository repo;

    public GetCardByStripeIdUseCase(CardRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves a single card
     * Accepts a card id from controller, fetches the card from the repository,
     *
     * @param stripeCardId the id of the card to be retrieved
     * @return the retrieved card entity
     */
    public Card execute(String stripeCardId) {
        return repo.findByStripeCardIdAndIsDeletedFalse(stripeCardId).orElseThrow(CardNotFoundException::new);
    }
}
