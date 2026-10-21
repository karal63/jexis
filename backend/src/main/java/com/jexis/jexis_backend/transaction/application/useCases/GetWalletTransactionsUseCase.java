package com.jexis.jexis_backend.transaction.application.useCases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;

import java.util.List;
import java.util.UUID;

@Service
public class GetWalletTransactionsUseCase {

    private final TransactionRepository repo;

    public GetWalletTransactionsUseCase(TransactionRepository transactionRepository) {
        this.repo = transactionRepository;
    }

    public List<Transaction> execute(UUID walletId) {
        return repo.findByWalletId(walletId);
    }

    public org.springframework.data.domain.Page<Transaction> execute(UUID walletId, int page, int pageSize,
                                                                     String search, TransactionType type,
                                                                     TransactionStatus status, TransactionDirection direction,
                                                                     String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return repo.findWalletTransactionsWithFilters(pageable, walletId, normalize(search), type, status, direction);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "amount" -> "amount";
            case "status" -> "status";
            case "type" -> "type";
            case "direction" -> "direction";
            case "bankname" -> "bankName";
            case "merchantname" -> "merchantName";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
