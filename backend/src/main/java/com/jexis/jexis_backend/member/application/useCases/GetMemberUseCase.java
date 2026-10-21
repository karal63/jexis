package com.jexis.jexis_backend.member.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.exceptions.MemberNotFoundException;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
public class GetMemberUseCase {
    private final MemberRepository repo;

    public GetMemberUseCase(MemberRepository repo) {
        this.repo = repo;
    }

    public Member execute(UUID id) {
        Member member = repo.findById(id).orElseThrow(() -> new MemberNotFoundException());
        return member;
    }
    public Member execute(UUID accountId, UUID memberId) {
        Member member = execute(memberId);
        if (!member.getAccount().getId().equals(accountId)) {
            throw new MemberNotFoundException();
        }
        return member;
    }
}
