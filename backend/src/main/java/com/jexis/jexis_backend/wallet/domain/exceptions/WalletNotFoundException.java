package com.jexis.jexis_backend.wallet.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * WalletNotFoundException
 *
 * This exception is thrown when the requested wallet is not found. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class WalletNotFoundException extends DomainException {
    public WalletNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "WALLET_NOT_FOUND", "Wallet not found");
    }

}
