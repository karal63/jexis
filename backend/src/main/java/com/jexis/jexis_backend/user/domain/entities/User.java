package com.jexis.jexis_backend.user.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jexis.jexis_backend.user.domain.enums.UserRole;

import jakarta.persistence.*;

/**
 * User entity mapped to the persistence layer.
 * <p>
 * Represents a user record stored in the database and defines
 * its persistence structure (table mapping, constraints, and identifiers).
 * <p>
 * This class is managed by JPA and is used to persist and retrieve
 * user data.
 * <p>
 * Author: Leo
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<UserRole> roles = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActivated = false;

    @Column(unique = true)
    private String activationTokenHash;

    @Column(unique = true)
    private String stripeCustomerId;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public User(String firstName, String lastName, String email, String phoneNumber, String password, String activationTokenHash, List<UserRole> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.isActivated = false;
        this.activationTokenHash = activationTokenHash;
        this.roles = roles;
    }
}
