package com.jexis.jexis_backend.member.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
public class CanAccessUseCase {

    private final MemberRepository repo;

    public CanAccessUseCase(MemberRepository repo) {
        this.repo = repo;
    }

    public Boolean execute(UUID userId, UUID accountId) {
        return repo.existsByUserIdAndAccountId(userId, accountId);
    }
}
