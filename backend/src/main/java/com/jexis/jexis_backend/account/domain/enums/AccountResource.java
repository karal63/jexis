package com.jexis.jexis_backend.account.domain.enums;

import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;

public enum AccountResource {
    CARDS("cards", "max_cards"),
    MEMBERS("members", "max_members"),
    CARDHOLDERS("cardholders", "max_members"),
    WALLETS("wallets", "max_wallets");

    private final String path;
    private final String entitlementKey;

    AccountResource(String path, String entitlementKey) {
        this.path = path;
        this.entitlementKey = entitlementKey;
    }

    public String entitlementKey() { return entitlementKey; }

    public static AccountResource fromPath(String path) {
        for (AccountResource resource : values()) {
            if (resource.path.equals(path)) return resource;
        }
        throw new ResourceLimitException(400, "INVALID_RESOURCE", "Resource must be cards, members, cardholders or wallets");
    }
}
