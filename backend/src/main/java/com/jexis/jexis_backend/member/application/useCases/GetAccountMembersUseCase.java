package com.jexis.jexis_backend.member.application.useCases;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
public class GetAccountMembersUseCase {
    private final MemberRepository memberRepo;

    public GetAccountMembersUseCase(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    public List<Member> execute(UUID accountId) {
        return memberRepo.findAllByAccountId(accountId);
    }

    public Page<Member> execute(UUID accountId, int page, int pageSize, String search, Role role, String sortBy,
            String sortDirection) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return memberRepo.findAccountMembersWithFilters(pageable, accountId, normalize(search), role);
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "firstname", "firstName" -> "user.firstName";
            case "lastname", "lastName" -> "user.lastName";
            case "email" -> "user.email";
            case "role" -> "role";
            default -> "id";
        };

        return Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
