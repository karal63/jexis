package com.jexis.jexis_backend.cardholder.application.security;

import java.util.Optional;
import java.util.UUID;

import com.jexis.jexis_backend.card.application.useCases.GetCardUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.GetCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import org.springframework.stereotype.Component;

import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;

@Component
public class CardHolderAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final GetCardHolderUseCase getCardHolderUseCase;

    public CardHolderAuthorization(HasRoleUseCase hasRoleUseCase, GetCardHolderUseCase getCardHolderUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.getCardHolderUseCase = getCardHolderUseCase;
    }

    public boolean canView(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canView(UUID userId, UUID accountId,  UUID cardHolderId) {
        CardHolder cardHolder = getCardHolderUseCase.execute(cardHolderId);

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN))
                && cardHolder.getAccount().getId().equals(accountId);
    }

    public boolean canCreate(UUID userId, UUID accountId) {
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    public boolean canEdit(UUID userId, UUID accountId,  UUID cardHolderId) {
        CardHolder cardHolder = getCardHolderUseCase.execute(cardHolderId);

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN))
                && cardHolder.getAccount().getId().equals(accountId);
    }

    public boolean canDelete(UUID userId, UUID accountId,  UUID cardHolderId) {
        CardHolder cardHolder = getCardHolderUseCase.execute(cardHolderId);

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN))
                && cardHolder.getAccount().getId().equals(accountId);
    }

}
