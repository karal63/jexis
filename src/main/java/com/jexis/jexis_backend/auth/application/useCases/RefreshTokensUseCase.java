package com.jexis.jexis_backend.auth.application.useCases;

import com.jexis.jexis_backend.common.ratelimit.RateLimitService;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.auth.application.dto.LoginResult;
import com.jexis.jexis_backend.auth.application.dto.TokenPair;
import com.jexis.jexis_backend.auth.domain.exception.InvalidRtException;
import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotFoundException;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;

import io.jsonwebtoken.Claims;

/**
 * RefreshTokensUseCase
 *
 * This service class implements the use case for refreshing authentication
 * tokens.
 * It contains only the business logic related to token refresh, such as
 * validating the refresh token and generating new access and refresh tokens.
 *
 * Author: Leo
 */
@Service
public class RefreshTokensUseCase {
    private final RateLimitService rateLimits;
    JwtUtil jwtUtil;
    UserRepository repo;

    public RefreshTokensUseCase(JwtUtil jwtUtil, UserRepository repo, RateLimitService rateLimits) {
        this.rateLimits = rateLimits;
        this.jwtUtil = jwtUtil;
        this.repo = repo;
    }

    /*
     * Refreshes authentication tokens
     *
     * Accepts a refresh token from the client, validates it, and generates
     * new access and refresh tokens.
     *
     * @param token the refresh token
     * 
     * @return a {@link LoginResult} containing the authenticated user and token
     * pair
     */
    public LoginResult execute(String token) {
        if (!jwtUtil.validateRefreshToken(token)) {
            throw new InvalidRtException();
        }

        Claims claims = jwtUtil.extractClaimsRt(token);

        rateLimits.check("refresh-user", claims.get("id", String.class));

        Optional<User> user = repo.findById(UUID.fromString(claims.get("id", String.class)));

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        TokenPair tokens = jwtUtil.generateTokens(user.get().getId(), user.get().getFirstName(),
                user.get().getEmail(),
                user.get().getIsActivated(), user.get().getRoles());

        UserResponseDto userResponse = new UserResponseDto(user.get().getId(), user.get().getFirstName(),
                user.get().getLastName(), user.get().getEmail(),
                user.get().getPhoneNumber(), user.get().getRoles(), user.get().getIsActivated(),
                user.get().getCreatedAt(),
                user.get().getUpdatedAt());

        return new LoginResult(userResponse, tokens);
    }
}
