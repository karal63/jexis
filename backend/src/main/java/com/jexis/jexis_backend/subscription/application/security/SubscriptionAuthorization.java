package com.jexis.jexis_backend.subscription.application.security;

import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.subscription.application.useCases.GetSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.user.application.security.UserAuthorization;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SubscriptionAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final UserAuthorization userAuthorization;
    private final GetUserUseCase getUserUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;

    public SubscriptionAuthorization(HasRoleUseCase hasRoleUseCase, UserAuthorization userAuthorization, GetUserUseCase getUserUseCase, GetSubscriptionUseCase getSubscriptionUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.userAuthorization = userAuthorization;
        this.getUserUseCase = getUserUseCase;
        this.getSubscriptionUseCase = getSubscriptionUseCase;
    }

    public boolean canCheckout(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isAdmin(userId);
    }

    public boolean canCreate(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isAdmin(userId);
    }

    public boolean canUpdate(UUID userId, UUID accountId) {
        Subscription subscription = getSubscriptionUseCase.execute(accountId);

        return hasRoleUseCase.execute(userId, subscription.getAccount().getId(), Role.OWNER)
                || hasRoleUseCase.execute(userId, subscription.getAccount().getId(), Role.ADMIN)
                || isAdmin(userId);
    }

    public boolean canView(UUID userId, UUID subscriptionId) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);

        return hasRoleUseCase.execute(userId, subscription.getAccount().getId(), Role.OWNER)
                || hasRoleUseCase.execute(userId, subscription.getAccount().getId(), Role.ADMIN)
                || isAdmin(userId);
    }

    public boolean canViewActive(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isAdmin(userId);
    }

    private boolean isAdmin(UUID userId) {
        User user = getUserUseCase.execute(userId);
        return userAuthorization.isAdmin(user.getRoles());
    }
}
