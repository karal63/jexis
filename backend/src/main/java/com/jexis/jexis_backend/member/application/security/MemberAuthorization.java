package com.jexis.jexis_backend.member.application.security;

import java.util.UUID;

import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;
import com.jexis.jexis_backend.member.application.useCases.GetMembersUseCase;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.jexis.jexis_backend.member.application.useCases.CanAccessUseCase;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Component
public class MemberAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final CanAccessUseCase canAccessUseCase;
    private final GetMembersUseCase getMembersUseCase;
    private final GetMemberUseCase getMemberUseCase;

    public MemberAuthorization(HasRoleUseCase hasRoleUseCase,
                               CanAccessUseCase canAccessUseCase, GetMembersUseCase getMembersUseCase, GetMemberUseCase getMemberUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.canAccessUseCase = canAccessUseCase;
        this.getMembersUseCase = getMembersUseCase;
        this.getMemberUseCase = getMemberUseCase;
    }

    public boolean canView(UUID userId, UUID accountId) {
        return canAccessUseCase.execute(userId, accountId);
    }

    public boolean canView(UUID userId, UUID accountId, UUID memberId) {
        Member member = getMemberUseCase.execute(memberId);
        return canAccessUseCase.execute(userId, accountId) && member.getAccount().getId().equals(accountId);
    }

    public boolean canCreate(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canEdit(UUID userId, UUID accountId, UUID memberId) {
        Member member = getMemberUseCase.execute(memberId);

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN))
                && member.getAccount().getId().equals(accountId);
    }

    public boolean canDelete(UUID userId, UUID accountId, UUID memberId) {
        Member member = getMemberUseCase.execute(memberId);

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || userId.equals(member.getUser().getId())) && member.getAccount().getId().equals(accountId);
    }
}
