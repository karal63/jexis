package com.jexis.jexis_backend.auth.infrastructure;

import com.jexis.jexis_backend.auth.domain.entities.AuthSession;
import com.jexis.jexis_backend.user.domain.enums.UserRole;
import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    interface PrincipalRow {
        UUID getUserId();
        String getName();
        String getEmail();
        Boolean getActivated();
        UserRole getRole();
    }

    // Scalars avoid caching session/user entities in the request's OpenEntityManagerInView
    // before a later refresh transaction acquires its lock.
    @Query("""
            select u.id as userId, u.firstName as name, u.email as email,
                   u.isActivated as activated, role as role
            from AuthSession s join s.user u left join u.roles role
            where s.id = :sessionId and u.id = :userId and u.isDeleted = false
              and s.revokedAt is null and s.expiresAt > :now
            """)
    List<PrincipalRow> findPrincipal(@Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId, @Param("now") Instant now);

    @Query("""
            select s from AuthSession s join fetch s.user u
            where s.id = :sessionId and u.id = :userId and u.isDeleted = false
              and s.revokedAt is null and s.expiresAt > :now
            """)
    Optional<AuthSession> findActive(@Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query("update AuthSession s set s.revokedAt = :now where s.user.id = :userId and s.revokedAt is null")
    void revokeAll(@Param("userId") UUID userId, @Param("now") Instant now);
}
