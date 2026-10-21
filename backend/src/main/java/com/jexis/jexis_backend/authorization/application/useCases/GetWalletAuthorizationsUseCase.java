package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetWalletAuthorizationsUseCase {
    private final AuthorizationRepository authorizationRepository;

    public GetWalletAuthorizationsUseCase(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    public List<Authorization> execute(UUID walletId) {
        return authorizationRepository.findByWalletId(walletId);
    }

    public Page<Authorization> execute(UUID walletId, int page, int pageSize, String search, Boolean approved, AuthorizationStatus status, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return authorizationRepository.findWalletAuthorizationsWithFilters(walletId, pageable, search, approved, status);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "amount" -> "amount";
            case "status" -> "status";
            case "merchantname" -> "merchantname";
            case "merchantcategory" -> "merchantcategory";
            case "merchantcity" -> "merchantcity";
            case "merchantcountry" -> "merchantcountry";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }
}
