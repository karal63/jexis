package com.jexis.jexis_backend.authorization.application.security;

import com.jexis.jexis_backend.authorization.application.useCases.GetAuthorizationUseCase;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthorizationAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final GetAuthorizationUseCase getAuthorizationUseCase;
    private final GetWalletUseCase getWalletUseCase;

    public AuthorizationAuthorization(HasRoleUseCase hasRoleUseCase, GetAuthorizationUseCase getAuthorizationUseCase, GetWalletUseCase getWalletUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.getAuthorizationUseCase = getAuthorizationUseCase;
        this.getWalletUseCase = getWalletUseCase;
    }

    public boolean canView(UUID userId, UUID authorizationId) {
        Authorization authorization = getAuthorizationUseCase.execute(authorizationId);

        UUID accountId = authorization.getWallet().getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isCardTransactionOwner(authorization, userId);
    }

    public boolean canViewWallet(UUID userId, UUID walletId) {
        Wallet wallet = getWalletUseCase.execute(walletId);
        UUID accountId = wallet.getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    private boolean isCardTransactionOwner(Authorization authorization, UUID userId) {
        return  authorization.getCard().getUser().getId().equals(userId);
    }
}
