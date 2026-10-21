package com.jexis.jexis_backend.member.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.member.application.dto.EditMemberDto;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.exceptions.MemberNotFoundException;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
public class EditMemberUseCase {
    private final MemberRepository repo;

    public EditMemberUseCase(MemberRepository repo) {
        this.repo = repo;
    }

    public Member execute(UUID id, EditMemberDto body) {
        Member member = repo.findById(id).orElseThrow(() -> new MemberNotFoundException());

        if (body.role() != null) {
            member.setRole(body.role());
        }

        Member newMember = repo.save(member);

        return newMember;
    }

}
