package com.jexis.jexis_backend.wallet.domain.entities;

import com.jexis.jexis_backend.wallet.domain.WalletCurrency;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jexis.jexis_backend.account.domain.entities.Account;

import jakarta.persistence.*;

/**
 * Wallet entity mapped to the persistence layer.
 * <p>
 * Represents a wallet record stored in the database and defines
 * its persistence structure (table mapping, constraints, and identifiers).
 * <p>
 * This class is managed by JPA and is used to persist and retrieve
 * wallet data.
 * <p>
 * Author: Leo
 */
@Entity
@Table(name = "wallets")
public class Wallet {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(nullable = false)
    private String stripeFinancialAccountId;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @Getter
    @Setter
    @Column(nullable = false)
    private Long availableBalance = 0L;

    /** Actual denomination of the balance, which is stored in minor units. */
    @Getter
    @Column(nullable = false, length = 3, updatable = false)
    private String currency = WalletCurrency.DEFAULT;

    @Getter
    @Setter
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Getter
    @Setter
    @Column
    private LocalDateTime deletedAt;

    Wallet() {
    }

    public Wallet(String name, String stripeFinancialAccountId, Account account) {
        this.name = name;
        this.stripeFinancialAccountId = stripeFinancialAccountId;
        this.account = account;
    }
}
