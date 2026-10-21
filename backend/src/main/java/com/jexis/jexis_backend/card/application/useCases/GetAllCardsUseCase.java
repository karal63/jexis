package com.jexis.jexis_backend.card.application.useCases;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;

/**
 * GetAllCardsUseCase
 *
 * This service class implements the use case for retrieving all cards.
 * It contains only the business logic related to fetching cards, such as
 * interacting with the repository to fetch all persisted cards.
 *
 * Author: Leo
 */
@Service
public class GetAllCardsUseCase {
    private final CardRepository repo;

    public GetAllCardsUseCase(CardRepository repo) {
        this.repo = repo;
    }

    /*
     * Retrieves all cards
     *
     * Returns a list of all card entities stored in the repository.
     *
     * @return a list of all card entities
     */
    public List<Card> execute() {
        return repo.findAll();
    }

    public Page<Card> execute(int page, int pageSize, String search, CardStatus status, String brand, String type,
            String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return repo.findCardsWithFilters(pageable, normalize(search), status, normalize(brand), normalize(type));
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "last4" -> "last4";
            case "status" -> "status";
            case "brand" -> "brand";
            case "type" -> "type";
            case "cardholder" -> "cardHolder.name";
            case "wallet", "treasuryaccount" -> "treasuryAccount.name";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
