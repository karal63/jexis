package com.jexis.jexis_backend.auth.application.security;

import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.auth.application.dto.TokenPair;
import com.jexis.jexis_backend.auth.domain.entities.AuthSession;
import com.jexis.jexis_backend.auth.domain.exception.InvalidRtException;
import com.jexis.jexis_backend.auth.infrastructure.AuthSessionRepository;
import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;
import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.enums.UserRole;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private final AuthSessionRepository repository;
    private final UserRepository users;
    private final JwtUtil jwt;

    public record Rotation(User user, TokenPair tokens) {}

    @Transactional
    public TokenPair create(UUID userId) {
        User user = users.findForSessionUpdate(userId).orElseThrow(InvalidRtException::new);
        UUID sessionId = UUID.randomUUID();
        TokenPair tokens = tokens(user, sessionId);
        repository.save(new AuthSession(sessionId, user, HashUtils.sha256(tokens.getRefreshToken()),
                jwt.extractClaimsRt(tokens.getRefreshToken()).getExpiration().toInstant()));
        return tokens;
    }

    // A replay must commit the revocation even though the caller receives an error.
    @Transactional(noRollbackFor = InvalidRtException.class)
    public Rotation rotate(String token, Claims claims) {
        UUID userId = claimId(claims, "id");
        UUID sessionId = claimId(claims, "sid");
        User user = users.findForSessionUpdate(userId).orElseThrow(InvalidRtException::new);
        AuthSession session = repository.findActive(sessionId, userId, Instant.now())
                .orElseThrow(InvalidRtException::new);
        if (!session.getRefreshTokenHash().equals(HashUtils.sha256(token))) {
            // A previously issued token was reused: invalidate this device's whole session.
            session.setRevokedAt(Instant.now());
            throw new InvalidRtException();
        }
        TokenPair tokens = tokens(user, sessionId);
        session.setRefreshTokenHash(HashUtils.sha256(tokens.getRefreshToken()));
        session.setExpiresAt(jwt.extractClaimsRt(tokens.getRefreshToken()).getExpiration().toInstant());
        return new Rotation(user, tokens);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> authenticate(Claims claims) {
        UUID userId = claimId(claims, "id");
        UUID sessionId = claimId(claims, "sid");
        var rows = repository.findPrincipal(sessionId, userId, Instant.now());
        if (rows.isEmpty()) return Optional.empty();
        var user = rows.get(0);
        List<UserRole> roles = rows.stream().map(AuthSessionRepository.PrincipalRow::getRole)
                .filter(java.util.Objects::nonNull).toList();
        return Optional.of(new AuthUser(user.getUserId(), user.getName(), user.getEmail(),
                user.getActivated(), roles));
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        Claims claims;
        try {
            claims = jwt.extractClaimsRt(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            try {
                claims = jwt.extractClaimsAt(accessToken);
            } catch (JwtException | IllegalArgumentException invalidAccess) {
                return;
            }
        }
        UUID userId;
        UUID sessionId;
        try {
            userId = claimId(claims, "id");
            sessionId = claimId(claims, "sid");
        } catch (InvalidRtException ex) {
            return; // Legacy cookies are cleared by the controller too.
        }
        if (users.findForSessionUpdate(userId).isEmpty()) return;
        repository.findActive(sessionId, userId, Instant.now())
                .ifPresent(session -> session.setRevokedAt(Instant.now()));
    }

    @Transactional
    public void revokeAll(UUID userId) {
        // Use the same lock as login/refresh so no session escapes a concurrent revocation.
        users.findForSessionUpdate(userId).orElseThrow(InvalidRtException::new);
        repository.revokeAll(userId, Instant.now());
    }

    private TokenPair tokens(User user, UUID sessionId) {
        return jwt.generateTokens(user.getId(), user.getFirstName(), user.getEmail(),
                user.getIsActivated(), user.getRoles(), sessionId);
    }

    private UUID claimId(Claims claims, String name) {
        try {
            return UUID.fromString(claims.get(name, String.class));
        } catch (IllegalArgumentException | NullPointerException | JwtException ex) {
            throw new InvalidRtException();
        }
    }
}
