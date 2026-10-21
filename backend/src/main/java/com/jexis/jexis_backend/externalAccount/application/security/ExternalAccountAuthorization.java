package com.jexis.jexis_backend.externalAccount.application.security;

import com.jexis.jexis_backend.externalAccount.application.useCases.GetExternalAccountUseCase;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.user.application.security.UserAuthorization;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExternalAccountAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final GetExternalAccountUseCase getExternalAccountUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UserAuthorization userAuthorization;

    public ExternalAccountAuthorization(
            HasRoleUseCase hasRoleUseCase,
            GetExternalAccountUseCase getExternalAccountUseCase,
            GetUserUseCase getUserUseCase,
            UserAuthorization userAuthorization) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.getExternalAccountUseCase = getExternalAccountUseCase;
        this.getUserUseCase = getUserUseCase;
        this.userAuthorization = userAuthorization;
    }

    public boolean canViewAll(UUID userId) {
        return isAdmin(userId);
    }

    public boolean canView(UUID userId, UUID externalAccountId) {
        if (isAdmin(userId)) {
            return true;
        }

        ExternalAccount externalAccount = getExternalAccountUseCase.execute(externalAccountId);
        UUID accountId = externalAccount.getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canViewAccountExternalAccounts(UUID userId, UUID accountId) {
        if (isAdmin(userId)) {
            return true;
        }

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    private boolean isAdmin(UUID userId) {
        User user = getUserUseCase.execute(userId);
        return userAuthorization.isAdmin(user.getRoles());
    }
}
