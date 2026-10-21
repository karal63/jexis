package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;
import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;

@Service
public class GetMemberAuthorizationsUseCase {
    private final AuthorizationRepository authorizationRepository;
    private final GetMemberUseCase getMemberUseCase;

    public GetMemberAuthorizationsUseCase(AuthorizationRepository authorizationRepository, GetMemberUseCase getMemberUseCase) {
        this.getMemberUseCase = getMemberUseCase;
        this.authorizationRepository = authorizationRepository;
    }

    public Page<Authorization> execute(UUID accountId, UUID memberId, int page, int pageSize, String search, Boolean approved, AuthorizationStatus status, String sortBy, String sortDirection) {
        UUID userId = getMemberUseCase.execute(accountId, memberId).getUser().getId();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return authorizationRepository.findMemberAuthorizationsWithFilters(accountId, userId, pageable, normalize(search), approved, status);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "amount" -> "amount";
            case "status" -> "status";
            case "merchantname" -> "merchantName";
            case "merchantcategory" -> "merchantCategory";
            case "merchantcity" -> "merchantCity";
            case "merchantcountry" -> "merchantCountry";
            default -> "createdAt";
        };

        return Sort.by(direction, property);
    }
    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
