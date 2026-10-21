package com.jexis.jexis_backend.wallet.application.useCases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;

import java.util.List;

/**
 * GetAllWalletsUseCase
 */
@Service
public class GetAllWalletsUseCase {
    WalletRepository repo;

    public GetAllWalletsUseCase(WalletRepository repo) {
        this.repo = repo;
    }

    public List<Wallet> execute() {
        return repo.findAll();
    }

    public Page<Wallet> execute(int page, int pageSize, String search, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return repo.findWalletsWithFilters(pageable, normalize(search));
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "name" -> "name";
            case "availablebalance", "balance" -> "availableBalance";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
