package com.jexis.jexis_backend.wallet.application.useCases;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.stripe.application.useCases.CloseStripeWalletUseCase;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;

/**
 * DeleteWalletUseCase
 * This service class implements the use case for deleting a wallet.
 * It contains only the business logic related to wallet deletion, such as
 * validating input data and interacting with the repository to update the
 * wallet.
 * Author: Leo
 */
@Service
public class CloseWalletUseCase {
    private final GetWalletUseCase getWalletUseCase;
    private final WalletRepository repo;
    private final CloseStripeWalletUseCase closeStripeWalletUseCase;
    GetAccountUseCase getAccountUseCase;

    public CloseWalletUseCase(GetWalletUseCase getWalletUseCase, WalletRepository walletRepository, CloseStripeWalletUseCase closeStripeWalletUseCase,
                              GetAccountUseCase getAccountUseCase) {
        this.getWalletUseCase = getWalletUseCase;
        this.repo = walletRepository;
        this.closeStripeWalletUseCase = closeStripeWalletUseCase;
        this.getAccountUseCase = getAccountUseCase;
    }

    /**
     * Deletes a wallet
     *
     * @param accountId the account id
     * @param walletId  the delete wallet id
     */
    public void execute(UUID accountId, UUID walletId) {
        Account account = getAccountUseCase.execute(accountId);
        Wallet wallet = getWalletUseCase.execute(walletId);

        closeStripeWalletUseCase.execute(account.getConnectAccountId(), wallet.getStripeFinancialAccountId());

        wallet.setIsDeleted(true);
        wallet.setDeletedAt(LocalDateTime.now());

        repo.save(wallet);
    }
}
