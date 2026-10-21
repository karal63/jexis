package com.jexis.jexis_backend.wallet.application.useCases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.stripe.application.useCases.UpdateStripeWalletUseCase;
import com.jexis.jexis_backend.wallet.application.dto.EditWalletDto;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.domain.exceptions.WalletNotFoundException;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;

/**
 * EditWalletUseCase
 *
 * This service class implements the use case for editing a wallet.
 * It contains only the business logic related to wallet modification, such as
 * validating input data and interacting with the repository to update the
 * wallet.
 *
 * Author: Leo
 */
@Service
public class EditWalletUseCase {
    private final WalletRepository repo;
    private final UpdateStripeWalletUseCase updateStripeWalletUseCase;

    public EditWalletUseCase(WalletRepository repo, UpdateStripeWalletUseCase updateStripeWalletUseCase) {
        this.repo = repo;
        this.updateStripeWalletUseCase = updateStripeWalletUseCase;
    }

    /*
     * Edits a wallet
     *
     * Accepts a {@link EditWalletDto} payload from controller, validates the
     * request,
     * and updates the wallet with the new information.
     *
     * @param id the ID of the wallet to edit
     * 
     * @param dto the edit data transfer object containing updated wallet details
     * 
     * @return the updated wallet
     */
    public Wallet execute(UUID id, EditWalletDto dto) {
        Wallet wallet = repo.findById(id).orElseThrow(WalletNotFoundException::new);

        if (dto.getName() != null) {
            wallet.setName(dto.getName());
            updateStripeWalletUseCase.execute(wallet.getAccount().getConnectAccountId(),
                    wallet.getStripeFinancialAccountId(), dto);
        }

        return repo.save(wallet);
    }
}
