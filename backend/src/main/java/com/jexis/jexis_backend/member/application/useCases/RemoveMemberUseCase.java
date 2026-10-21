package com.jexis.jexis_backend.member.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
public class RemoveMemberUseCase {
    private final MemberRepository repo;

    public RemoveMemberUseCase(MemberRepository repo) {
        this.repo = repo;
    }

    public void execute(UUID id) {
        repo.deleteById(id);
    }
}
