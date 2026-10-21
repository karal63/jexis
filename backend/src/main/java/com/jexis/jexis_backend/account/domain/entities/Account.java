package com.jexis.jexis_backend.account.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jexis.jexis_backend.user.domain.entities.User;

import jakarta.persistence.*;

/**
 * Account entity mapped to the persistence layer.
 * <p>
 * Represents an account record stored in the database and defines
 * its persistence structure (table mapping, constraints, and identifiers).
 * <p>
 * This class is managed by JPA and is used to persist and retrieve
 * account data.
 * <p>
 * Author: Leo
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String city;

    @Column
    private String country;

    @Column
    private String line1;

    @Column
    private String line2;

    @Column
    private String postalCode;

    @Column
    private String state;

    @Column
    private String phone;

    @Column
    private String email;

    @Column(nullable = false, unique = true)
    private String connectAccountId;

    @Column(nullable = false, unique = true)
    private String accountLink;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private User owner;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Account(String email, String connectAccountId, String accountLink, User owner) {
        this.email = email;
        this.connectAccountId = connectAccountId;
        this.accountLink = accountLink;
        this.owner = owner;
    }
}
