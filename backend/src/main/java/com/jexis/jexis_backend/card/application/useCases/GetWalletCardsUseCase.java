package com.jexis.jexis_backend.card.application.useCases;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;

@Service
public class GetWalletCardsUseCase {
    private final CardRepository cardRepo;

    public GetWalletCardsUseCase(CardRepository cardRepo) {
        this.cardRepo = cardRepo;
    }

    public List<Card> execute(UUID walletId) {
        return cardRepo.findAllByTreasuryAccountIdAndIsDeletedFalse(walletId);
    }

    public Page<Card> execute(UUID walletId, int page, int pageSize, String search, CardStatus status, String brand,
            String type, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return cardRepo.findWalletCardsWithFilters(pageable, walletId, normalize(search), status, normalize(brand),
                normalize(type));
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
