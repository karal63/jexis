package com.jexis.jexis_backend.dispute.application.security;

import com.jexis.jexis_backend.card.application.useCases.GetCardUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.dispute.application.useCases.GetDisputeUseCase;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.user.application.security.UserAuthorization;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DisputeAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final GetDisputeUseCase getDisputeUseCase;
    private final GetCardUseCase getCardUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UserAuthorization userAuthorization;
    private final GetTransactionUseCase getTransactionUseCase;

    public boolean canViewAll(UUID userId) {
        return isAdmin(userId);
    }

    public boolean canCreateDispute(UUID userId, UUID transactionId) {
        if (isAdmin(userId)) {
            return true;
        }

        Transaction transaction = getTransactionUseCase.execute(transactionId);
        UUID accountId = transaction.getWallet().getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isCardOwner(transaction, userId);
    }

    public boolean canView(UUID userId, UUID disputeId) {
        if (isAdmin(userId)) {
            return true;
        }

        Dispute dispute = getDisputeUseCase.execute(disputeId);
        UUID accountId = dispute.getWallet().getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isCardOwner(dispute.getTransaction(), userId);
    }

    public boolean canViewCardDisputes(UUID userId, UUID cardId) {
        if (isAdmin(userId)) {
            return true;
        }

        Card card = getCardUseCase.execute(cardId);
        UUID accountId = card.getCardHolder().getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || card.getUser().getId().equals(userId);
    }

    private boolean isAdmin(UUID userId) {
        User user = getUserUseCase.execute(userId);
        return userAuthorization.isAdmin(user.getRoles());
    }

    private boolean isCardOwner(Transaction transaction, UUID userId) {
        return transaction.getCard() != null
                && transaction.getCard().getUser().getId().equals(userId);
    }
}
