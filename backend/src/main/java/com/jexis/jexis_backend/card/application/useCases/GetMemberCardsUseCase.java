package com.jexis.jexis_backend.card.application.useCases;

import java.util.UUID;
import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;

@Service
public class GetMemberCardsUseCase {
    private final CardRepository cardRepo;
    private final GetMemberUseCase getMemberUseCase;

    public GetMemberCardsUseCase(CardRepository cardRepo, GetMemberUseCase getMemberUseCase) {
        this.getMemberUseCase = getMemberUseCase;
        this.cardRepo = cardRepo;
    }

    public Page<Card> execute(UUID accountId, UUID memberId, int page, int pageSize, String search, CardStatus status, String brand,
            String type, String sortBy, String sortDirection) {
        UUID userId = getMemberUseCase.execute(accountId, memberId).getUser().getId();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return cardRepo.findMemberCardsWithFilters(pageable, accountId, userId, normalize(search), status, normalize(brand),
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
