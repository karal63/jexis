package com.jexis.jexis_backend.transaction.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.exceptions.TransactionNotFoundException;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;

/**
 * GetTransactionUseCase
 *
 * This service class implements the use case for retrieving a single
 * transaction by its identifier. It contains only the business logic related
 * to fetching a transaction, such as interacting with the repository and
 * handling not-found scenarios.
 *
 * Author: Copilot
 */
@Service
public class GetTransactionUseCase {

    private final TransactionRepository repo;

    public GetTransactionUseCase(TransactionRepository transactionRepository) {
        this.repo = transactionRepository;
    }

    /**
     * Handles fetching a single transaction by its ID.
     * Calls the repository to fetch the transaction and throws an exception
     * if the transaction is not found.
     *
     * @param id the unique identifier of the transaction to retrieve
     * @return the found transaction
     * @throws TransactionNotFoundException if transaction is not found
     */
    public Transaction execute(UUID id) {
        return repo.findById(id).orElseThrow(TransactionNotFoundException::new);
    }
}
