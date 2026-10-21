package com.jexis.jexis_backend.cardholder.application.useCases;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;

@Service
public class GetAllCardHoldersUseCase {
    private final CardHolderRepository repo;

    public GetAllCardHoldersUseCase(CardHolderRepository repo) {
        this.repo = repo;
    }

    public List<CardHolder> execute() {
        return repo.findAll();
    }

    public Page<CardHolder> execute(int page, int pageSize, String search, CardHolderStatus status, String sortBy,
            String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return repo.findCardHoldersWithFilters(pageable, normalize(search), status);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "id" -> "id";
            case "name" -> "name";
            case "status" -> "status";
            case "billingaddressline1" -> "billingAddressLine1";
            case "billingcity" -> "billingCity";
            case "billingstate" -> "billingState";
            case "billingcountry" -> "billingCountry";
            case "billingpostalcode" -> "billingPostalCode";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
