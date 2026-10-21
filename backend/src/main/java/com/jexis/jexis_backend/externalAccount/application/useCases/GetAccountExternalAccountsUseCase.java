package com.jexis.jexis_backend.externalAccount.application.useCases;

import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.externalAccount.infrastructure.ExternalAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetAccountExternalAccountsUseCase {
    private final ExternalAccountRepository externalAccountRepository;

    public GetAccountExternalAccountsUseCase(ExternalAccountRepository externalAccountRepository) {
        this.externalAccountRepository = externalAccountRepository;
    }

    public List<ExternalAccount> execute(UUID accountId) {
        return externalAccountRepository.findAll().stream()
                .filter(externalAccount -> externalAccount.getAccount().getId().equals(accountId))
                .filter(externalAccount -> !externalAccount.isDeleted())
                .toList();
    }

    public Page<ExternalAccount> execute(UUID accountId, int page, int pageSize, String search, Boolean isDefault,
            String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return externalAccountRepository.findAccountExternalAccountsWithFilters(pageable, accountId, normalize(search),
                isDefault);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "id" -> "id";
            case "bankname" -> "bankName";
            case "last4" -> "last4";
            case "country" -> "country";
            case "isdefault" -> "isDefault";
            case "createdat" -> "createdAt";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
