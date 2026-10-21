package com.jexis.jexis_backend.card.application.security;

import java.util.UUID;

import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import org.springframework.stereotype.Component;

import com.jexis.jexis_backend.card.application.useCases.GetCardUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;

@Component
public class CardAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final GetCardUseCase getCardUseCase;
    private final GetWalletUseCase getWalletUseCase;

    public CardAuthorization(HasRoleUseCase hasRoleUseCase, GetCardUseCase getCardUseCase, GetWalletUseCase getWalletUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.getCardUseCase = getCardUseCase;
        this.getWalletUseCase = getWalletUseCase;
    }

    public boolean canViewAll(UUID userId, UUID walletId) {
        Wallet wallet = getWalletUseCase.execute(walletId);

        return hasRoleUseCase.execute(userId, wallet.getAccount().getId(), Role.OWNER)
                || hasRoleUseCase.execute(userId, wallet.getAccount().getId(), Role.ADMIN);
    }

    public boolean canView(UUID userId, UUID cardId) {
        Card card = getCardUseCase.execute(cardId);
        UUID accountId = card.getTreasuryAccount().getAccount().getId();

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || card.getUser().getId().equals(userId))
                && card.getCardHolder().getAccount().getId().equals(accountId);
    }

    public boolean canRevealDetails(UUID userId, UUID cardId) {
        Card card = getCardUseCase.execute(cardId);
        return card.getUser().getId().equals(userId);
    }

    public boolean canCreate(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canEdit(UUID userId, UUID cardId) {
        Card card = getCardUseCase.execute(cardId);
        UUID accountId = card.getTreasuryAccount().getAccount().getId();

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN))
                && card.getCardHolder().getAccount().getId().equals(accountId);
    }

    public boolean canDelete(UUID userId, UUID cardId) {
        Card card = getCardUseCase.execute(cardId);
        UUID accountId = card.getTreasuryAccount().getAccount().getId();

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN))
                && card.getCardHolder().getAccount().getId().equals(accountId);
    }


}
