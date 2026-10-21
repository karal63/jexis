package com.jexis.jexis_backend.wallet.application.useCases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.stripe.application.useCases.CreateTreasuryAccount;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import com.stripe.model.treasury.FinancialAccount;

/**
 * CreateWalletUseCase
 *
 * This service class implements the use case for creating a new wallet.
 * It contains only the business logic related to wallet creation, such as
 * interacting with the repository to persist the new wallet.
 *
 * Author: Leo
 */
@Service
public class CreateWalletUseCase {
    private final WalletRepository repo;
    private final CreateTreasuryAccount createTreasuryAccount;
    private final LockAccountUseCase lockAccount;
    private final CheckAccountResourceLimitUseCase checkLimit;

    public CreateWalletUseCase(WalletRepository repo, CreateTreasuryAccount createTreasuryAccount,
            LockAccountUseCase lockAccount, CheckAccountResourceLimitUseCase checkLimit) {
        this.lockAccount = lockAccount;
        this.checkLimit = checkLimit;
        this.repo = repo;
        this.createTreasuryAccount = createTreasuryAccount;
    }

    /**
     * Creates a new wallet
     *
     * Accepts a {@link CreateDto} payload from controller, creates a new wallet,
     * and returns the created wallet.
     *
     * @param body the create data transfer object containing wallet details
     * 
     * @return the created wallet
     */
    @Transactional
    public Wallet execute(Account account, String walletName) {
        account = lockAccount.execute(account.getId());
        checkLimit.requireCapacity(account.getId(), AccountResource.WALLETS, 1);
        FinancialAccount financialAccount = createTreasuryAccount.execute(account.getConnectAccountId(), walletName);

        Wallet wallet = new Wallet(walletName, financialAccount.getId(), account);
        return repo.save(wallet);
    }
}
