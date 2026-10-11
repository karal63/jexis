package com.jexis.jexis_backend.transaction.application.security;

import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionAuthorization {
    private final HasRoleUseCase hasRoleUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final GetWalletUseCase getWalletUseCase;

    public TransactionAuthorization(HasRoleUseCase hasRoleUseCase, GetTransactionUseCase getTransactionUseCase, GetWalletUseCase getWalletUseCase) {
        this.hasRoleUseCase = hasRoleUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
        this.getWalletUseCase = getWalletUseCase;
    }

    public boolean canView(UUID userId, UUID transactionId) {
        Transaction transaction = getTransactionUseCase.execute(transactionId);

        UUID accountId = transaction.getWallet().getAccount().getId();

        return (hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || isCardTransactionOwner(transaction, userId));
    }

    public boolean canViewWallet(UUID userId, UUID walletId) {
        Wallet wallet = getWalletUseCase.execute(walletId);
        UUID accountId = wallet.getAccount().getId();

        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

    private boolean isCardTransactionOwner(Transaction transaction, UUID userId) {
        return transaction.getCard() != null && transaction.getCard().getUser().getId().equals(userId);
    }
}

