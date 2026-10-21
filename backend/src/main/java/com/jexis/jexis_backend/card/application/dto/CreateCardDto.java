package com.jexis.jexis_backend.card.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * CreateCardDto
 *
 * Data Transfer Object used for card creation requests. It encapsulates the
 * necessary data required to create a new card, such as the card details and
 * user ID.
 *
 * Author: Leo
 */
public class CreateCardDto {
    @NotNull(message = "Account id cannot be blank")
    private UUID accountId;

    @NotNull(message = "Card holder id cannot be blank")
    private UUID cardHolderId;

    @NotNull(message = "Wallet id cannot be blank")
    private UUID walletId;

    @NotNull(message = "User id cannot be blank")
    private UUID userId;

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCardHolderId() {
        return cardHolderId;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setCardHolderId(UUID cardHolderId) {
        this.cardHolderId = cardHolderId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    // private UUID cardHolderId;
    // private String last4;
    // private String status;
    // private BigDecimal limit;
    // private String brand;
    // private String type;
    // private String currency;
    // private Integer expYear;

    // public UUID getCardHolderId() {
    // return cardHolderId;
    // }

    // public void setCardHolderId(UUID cardHolderId) {
    // this.cardHolderId = cardHolderId;
    // }

    // public String getLast4() {
    // return last4;
    // }

    // public void setLast4(String last4) {
    // this.last4 = last4;
    // }

    // public String getStatus() {
    // return status;
    // }

    // public void setStatus(String status) {
    // this.status = status;
    // }

    // public BigDecimal getLimit() {
    // return limit;
    // }

    // public void setLimit(BigDecimal limit) {
    // this.limit = limit;
    // }

    // public String getBrand() {
    // return brand;
    // }

    // public void setBrand(String brand) {
    // this.brand = brand;
    // }

    // public String getType() {
    // return type;
    // }

    // public void setType(String type) {
    // this.type = type;
    // }

    // public String getCurrency() {
    // return currency;
    // }

    // public void setCurrency(String currency) {
    // this.currency = currency;
    // }

    // public Integer getExpYear() {
    // return expYear;
    // }

    // public void setExpYear(Integer expYear) {
    // this.expYear = expYear;
    // }
}
