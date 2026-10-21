package com.jexis.jexis_backend.wallet.domain;

import com.jexis.jexis_backend.wallet.domain.exceptions.WalletCurrencyException;
import java.util.Locale;

/** Currencies supported by the current wallet and issuing integration. */
public final class WalletCurrency {
    public static final String DEFAULT = "USD";

    private WalletCurrency() {}

    public static String toStripe(String currency) {
        requireSupported(currency);
        return currency.toLowerCase(Locale.ROOT);
    }

    public static void requireSupported(String currency) {
        if (!DEFAULT.equalsIgnoreCase(currency)) {
            throw new WalletCurrencyException("Only USD wallets are currently supported");
        }
    }

    public static void requireMatch(String walletCurrency, String currency) {
        requireSupported(walletCurrency);
        if (!walletCurrency.equalsIgnoreCase(currency)) {
            throw new WalletCurrencyException("Currency must match the wallet currency ("
                    + walletCurrency + "); currency conversion is not supported");
        }
    }
}
