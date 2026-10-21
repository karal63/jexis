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

import java.util.UUID;
import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;

@Service
public class GetMemberTransactionsUseCase {

    private final TransactionRepository repo;
    private final GetMemberUseCase getMemberUseCase;

    public GetMemberTransactionsUseCase(TransactionRepository transactionRepository, GetMemberUseCase getMemberUseCase) {
        this.getMemberUseCase = getMemberUseCase;
        this.repo = transactionRepository;
    }

    public Page<Transaction> execute(UUID accountId, UUID memberId, int page, int pageSize,
                                      String search, TransactionType type, TransactionStatus status,
                                      TransactionDirection direction, String sortBy, String sortDirection) {
        UUID userId = getMemberUseCase.execute(accountId, memberId).getUser().getId();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return repo.findMemberTransactionsWithFilters(pageable, accountId, userId, normalize(search), type, status, direction);
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
