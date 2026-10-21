package com.jexis.jexis_backend.transaction.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * TransactionNotFoundException
 *
 * This exception is thrown when the provided transaction is not found. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Copilot
 */
public class TransactionNotFoundException extends DomainException {
    public TransactionNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "TRANSACTION_NOT_FOUND", "Transaction not found");
    }
}
