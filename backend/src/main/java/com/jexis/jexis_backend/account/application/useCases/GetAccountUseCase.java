package com.jexis.jexis_backend.account.application.useCases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.exception.AccountNotFoundException;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;

/**
 * GetAccountUseCase
 * <p>
 * This service class implements the use case for retrieving a specific account.
 * It contains the business logic related to fetching an account,
 * such as interacting with the repository to fetch the data and validating if
 * user with given id exists
 * <p>
 * Author: Leo
 */
@Service
public class GetAccountUseCase {
    AccountRepository repo;

    public GetAccountUseCase(AccountRepository repo) {
        this.repo = repo;
    }

    /**
     * Handles fetching a specific account.
     * <p>
     * Calls the repository to fetch the account and returns the account.
     *
     * @param id the ID of the account to fetch
     * @return the fetched account
     */
    public Account execute(UUID id) {
        return repo.findByIdAndIsDeletedFalse(id).orElseThrow(AccountNotFoundException::new);
    }
}
