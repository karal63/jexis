package com.jexis.jexis_backend.dispute.application.useCases;

import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;
import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;

@Service
public class GetMemberDisputesUseCase {
    private final DisputeRepository disputeRepository;
    private final GetMemberUseCase getMemberUseCase;

    public GetMemberDisputesUseCase(DisputeRepository disputeRepository, GetMemberUseCase getMemberUseCase) {
        this.getMemberUseCase = getMemberUseCase;
        this.disputeRepository = disputeRepository;
    }

    public Page<Dispute> execute(UUID accountId, UUID memberId, int page, int pageSize, String search, DisputeReason reason,
            DisputeStatus status, String sortBy, String sortDirection) {
        UUID userId = getMemberUseCase.execute(accountId, memberId).getUser().getId();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return disputeRepository.findMemberDisputesWithFilters(pageable, accountId, userId, normalize(search), reason, status);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "id" -> "id";
            case "strikedisputeid" -> "stripeDisputeId";
            case "amount" -> "amount";
            case "currency" -> "currency";
            case "status" -> "status";
            case "reason" -> "reason";
            case "wallet", "walletname" -> "wallet.name";
            case "resolvedat" -> "resolvedAt";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
