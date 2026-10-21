package com.jexis.jexis_backend.wallet.application.useCases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.domain.exceptions.WalletNotFoundException;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;

/**
 * GetWalletUseCase
 *
 * This service class implements the use case for retrieving a single wallet.
 * It contains only the business logic related to fetching wallet data, such as
 * interacting with the repository to retrieve a specific wallet.
 *
 * Author: Leo
 */
@Service
public class GetWalletUseCase {
    WalletRepository repo;

    public GetWalletUseCase(WalletRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves a single wallet
     * Interacts with the repository to fetch a specific wallet by its ID.
     *
     * @param walletId the ID of the wallet to retrieve
     * @return the retrieved wallet
     */
    public Wallet execute(UUID walletId) {
        return repo.findByIdAndIsDeletedFalse(walletId).orElseThrow(WalletNotFoundException::new);
    }
}
