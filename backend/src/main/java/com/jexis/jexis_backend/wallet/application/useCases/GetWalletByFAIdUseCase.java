package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.domain.exceptions.WalletNotFoundException;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * GetWalletByFAIdUseCase
 * This service class implements the use case for retrieving a single wallet.
 * It contains only the business logic related to fetching wallet data, such as
 * interacting with the repository to retrieve a specific wallet.
 * Author: Leo
 */
@Service
public class GetWalletByFAIdUseCase {
    WalletRepository repo;

    public GetWalletByFAIdUseCase(WalletRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves a single wallet
     * Interacts with the repository to fetch a specific wallet by its ID.
     *
     * @param financialAccountId the ID of the Stripe financial account to retrieve
     * @return the retrieved wallet
     */
    public Wallet execute(String financialAccountId) {
        return repo.findByStripeFinancialAccountIdAndIsDeletedFalse(financialAccountId).orElseThrow(WalletNotFoundException::new);
    }
}
