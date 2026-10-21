package com.jexis.jexis_backend.externalAccount.domain.entities;

import com.jexis.jexis_backend.account.domain.entities.Account;
import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "externalAccounts")
public class ExternalAccount {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @Setter
    @Column(nullable = false, unique = true)
    private String stripeExternalAccountId;

    @Setter
    @Column(nullable = false)
    private String bankName;

    @Setter
    @Column(nullable = false)
    private String last4;

    @Setter
    @Column(nullable = false)
    private String country;

    @Setter
    @Column(nullable = false)
    private String currency;

    @Setter
    @Column(nullable = false)
    private boolean isDefault;

    @Setter
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Setter
    @Column()
    private LocalDateTime deletedAt;

    public ExternalAccount() {
    }

    public ExternalAccount(Account account, String stripeExternalAccountId, String bankName, String last4, String country, String currency, Boolean isDefault) {
        this.account = account;
        this.stripeExternalAccountId = stripeExternalAccountId;
        this.bankName = bankName;
        this.last4 = last4;
        this.country = country;
        this.currency = currency;
        this.isDefault = isDefault;
    }


    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getStripeExternalAccountId() {
        return stripeExternalAccountId;
    }

    public String getBankName() {
        return bankName;
    }

    public String getLast4() {
        return last4;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDeleted() {return isDeleted; }

    public void setIsDeleted(boolean isDeleted) {this.isDeleted = isDeleted; }

    public LocalDateTime getDeletedAt() {return deletedAt; }

}
