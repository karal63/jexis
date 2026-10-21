package com.jexis.jexis_backend.passwordResetToken.domain.entities;

import com.jexis.jexis_backend.user.domain.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Getter
    @Setter
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Getter
    @Setter
    private LocalDateTime usedAt;

    @Getter
    @Setter
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(User user, String tokenHash, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }
}
