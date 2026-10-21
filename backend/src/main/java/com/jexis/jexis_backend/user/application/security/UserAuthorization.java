package com.jexis.jexis_backend.user.application.security;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import org.springframework.stereotype.Component;

import com.jexis.jexis_backend.user.domain.enums.UserRole;

@Component
public class UserAuthorization {

    private final GetUserUseCase getUserUseCase;

    public UserAuthorization(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    public boolean canEdit(UUID requestingUserId, UUID targetingUserId) {
        User user = getUserUseCase.execute(requestingUserId);

        if (isAdmin(user.getRoles())) {
            return true;
        }

        return requestingUserId.equals(targetingUserId);
    }

    public boolean canDelete(UUID requestingUserId, UUID targetingUserId) {
        User user = getUserUseCase.execute(requestingUserId);

        if (isAdmin(user.getRoles())) {
            return true;
        }

        return requestingUserId.equals(targetingUserId);
    }

    public boolean isAdmin(List<UserRole> roles) {
        return roles.contains(UserRole.ADMIN);
    }

    public boolean isAdmin(UUID userId) {
        User user = getUserUseCase.execute(userId);
        return isAdmin(user.getRoles());
    }
}
