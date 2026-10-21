package com.jexis.jexis_backend.wallet.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class WalletCurrencyException extends DomainException {
    public WalletCurrencyException(String message) {
        super(400, "WALLET_CURRENCY_MISMATCH", message);
    }
}
