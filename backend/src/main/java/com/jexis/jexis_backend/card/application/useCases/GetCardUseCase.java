package com.jexis.jexis_backend.card.application.useCases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.exceptions.CardNotFoundException;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;

/**
 * GetCardUseCase
 *
 * This service class implements the use case for retrieving a single card.
 * It contains only the business logic related to fetching a card, such as
 * interacting with the repository to fetch the requested card.
 *
 * Author: Leo
 */
@Service
public class GetCardUseCase {
    private final CardRepository repo;

    public GetCardUseCase(CardRepository repo) {
        this.repo = repo;
    }

    /*
     * Retrieves a single card
     *
     * Accepts a card id from controller, fetches the card from the repository,
     *
     * @param id the id of the card to be retrieved
     * 
     * @return the retrieved card entity
     */
    public Card execute(UUID id) {
        return repo.findByIdAndIsDeletedFalse(id).orElseThrow(CardNotFoundException::new);
    }
}
