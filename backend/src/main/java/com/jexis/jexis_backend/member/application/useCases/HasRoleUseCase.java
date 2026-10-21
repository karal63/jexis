package com.jexis.jexis_backend.member.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
public class HasRoleUseCase {
    private final MemberRepository repo;

    public HasRoleUseCase(MemberRepository repo) {
        this.repo = repo;
    }

    public boolean execute(UUID userId, UUID accountId, Role role) {
        return repo.existsByUserIdAndAccountIdAndRole(userId, accountId, role);
    }
}
