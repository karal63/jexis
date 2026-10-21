package com.jexis.jexis_backend.account.application.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jexis.jexis_backend.member.application.useCases.CanAccessUseCase;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;

@Component
public class AccountAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final CanAccessUseCase canAccessUseCase;

    public AccountAuthorization(HasRoleUseCase hasRoleUseCase, CanAccessUseCase canAccessUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.canAccessUseCase = canAccessUseCase;
    }

    public boolean canView(UUID userId, UUID accountId) {
        return canAccessUseCase.execute(userId, accountId);
    }

    public boolean canCreate(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canEdit(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canDelete(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER);
    }
}
