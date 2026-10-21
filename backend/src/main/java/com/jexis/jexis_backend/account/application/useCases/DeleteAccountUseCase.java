package com.jexis.jexis_backend.account.application.useCases;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.stripe.application.useCases.DeleteStripeAccountUseCase;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.exception.AccountNotFoundException;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;

/**
 * DeleteAccountUseCase
 * This service class implements the use case for deleting an existing account.
 * It contains only the business logic related to account deletion, such as
 * validating the input ID and interacting with the repository to remove the
 * account.
 * Author: Leo
 */
@Service
public class DeleteAccountUseCase {
    AccountRepository repo;
    private final DeleteStripeAccountUseCase deleteStripeAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;

    public DeleteAccountUseCase(AccountRepository repo, DeleteStripeAccountUseCase deleteStripeAccountUseCase,
                                GetAccountUseCase getAccountUseCase) {
        this.repo = repo;
        this.deleteStripeAccountUseCase = deleteStripeAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
    }

    /**
     * Handles account deletion.
     * Accepts a {@link UUID} payload from controller, looks for
     * the account, calls the repository to delete the account, and returns
     * nothing.
     *
     * @param id passed by controller payload containing account creation data
     */
    public void execute(UUID id) {
        Account account = getAccountUseCase.execute(id);
        deleteStripeAccountUseCase.execute(account.getConnectAccountId());

        account.setIsDeleted(true);
        account.setDeletedAt(LocalDateTime.now());

        repo.save(account);
    }
}
