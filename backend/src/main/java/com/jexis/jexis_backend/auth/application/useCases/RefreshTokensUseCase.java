package com.jexis.jexis_backend.auth.application.useCases;

import com.jexis.jexis_backend.auth.application.dto.LoginResult;
import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import com.jexis.jexis_backend.auth.domain.exception.InvalidRtException;
import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;
import com.jexis.jexis_backend.common.ratelimit.RateLimitService;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokensUseCase {
    private final JwtUtil jwtUtil;
    private final AuthSessionService sessions;
    private final RateLimitService rateLimits;

    public LoginResult execute(String token) {
        Claims claims;
        UUID userId;
        try {
            claims = jwtUtil.extractClaimsRt(token);
            userId = UUID.fromString(claims.get("id", String.class));
        } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRtException();
        }
        rateLimits.check("refresh-user", userId.toString());
        var result = sessions.rotate(token, claims);
        User user = result.user();
        UserResponseDto response = new UserResponseDto(user.getId(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhoneNumber(), user.getRoles(),
                user.getIsActivated(), user.getCreatedAt(), user.getUpdatedAt());
        return new LoginResult(response, result.tokens());
    }
}
