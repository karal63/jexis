package com.jexis.jexis_backend.passwordResetToken.infrastructure;

import com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now "
            + "where t.id = :id and t.usedAt is null and t.expiresAt > :now")
    int consume(@Param("id") UUID id, @Param("now") LocalDateTime now);
}
