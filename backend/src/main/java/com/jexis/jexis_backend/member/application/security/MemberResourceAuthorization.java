package com.jexis.jexis_backend.member.application.security;

import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.user.application.security.UserAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberResourceAuthorization {
    private final GetMemberUseCase getMemberUseCase;
    private final HasRoleUseCase hasRoleUseCase;
    private final UserAuthorization userAuthorization;

    public boolean canView(UUID userId, UUID accountId, UUID memberId) {
        // Validate the route's company/member pair even for global administrators.
        Member member = getMemberUseCase.execute(accountId, memberId);
        return member.getUser().getId().equals(userId)
                || userAuthorization.isAdmin(userId)
                || hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }
}
