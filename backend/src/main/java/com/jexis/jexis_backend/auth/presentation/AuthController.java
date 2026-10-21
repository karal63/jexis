package com.jexis.jexis_backend.auth.presentation;

import com.jexis.jexis_backend.common.ratelimit.RateLimitService;

import java.time.Duration;

import com.jexis.jexis_backend.auth.application.dto.*;
import com.jexis.jexis_backend.auth.application.useCases.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jexis.jexis_backend.user.application.dto.CreateDto;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

/**
 * AuthController
 * REST controller in the presentation layer responsible for exposing
 * authentication-related HTTP endpoints.
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated authentication use case
 * services (application layer).
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 * Author: Leo
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final RateLimitService rateLimits;
    public final SignupUseCase signupUseCase;
    public final LoginUseCase loginUseCase;
    public final RefreshTokensUseCase refreshTokensUseCase;

    /**
     * Endpoint to create a new account (signup).
     * <p>
     * This endpoint accepts a JSON body with the necessary account details (e.g.,
     * email, password). It deligates the account creation logic to the
     * SignupUseCase, which handles all the necessary validations, password hashing,
     * and persistence.
     * <p>
     * Endpoint: POST /auth/signup
     *
     * @param body the request body containing the account details
     * @return the created account and tokens
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody CreateDto body) {
        SignupResult result = signupUseCase.execute(body);
        TokenPair tokens = result.tokens();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true) // set false only for local dev without HTTPS
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict") // or "Lax" / "None"
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.user());
    }

    /**
     * Endpoint to log out from existing account.
     * This endpoint clears the authentication cookies by setting them with empty
     * values and immediate expiration
     * Endpoint: GET /auth/logout
     *
     * @return a response with cleared cookies
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/") // MUST match original path
                .maxAge(0) // expire immediately
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/") // MUST match original path
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    /**
     * Endpoint to log in an existing account.
     * This endpoint accepts a JSON body with the login credentials (e.g., email and
     * password). It deligates the authentication logic to the LoginUseCase
     * Endpoint: POST /auth/login
     *
     * @param body the request body containing the login credentials
     * @return account and tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto body, HttpServletRequest request) {
        rateLimits.check("login-identity", request.getRemoteAddr() + "|"
                + RateLimitService.normalizeEmail(body.email()));
        LoginResult result = loginUseCase.execute(body);
        TokenPair tokens = result.tokens();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.user());
    }

    /**
     * Endpoint to refresh authentication tokens.
     * This endpoint accepts a refresh token. It deligates the token refresh logic
     * to the refreshTokensUseCase, which handles all the necessary validations,
     * password
     * hashing, and persistence.
     * Endpoint: POST /auth/refresh
     *
     * @param cookie refresh_token the request body containing the account details
     * @return account and tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token") String refreshToken) {
        LoginResult result = refreshTokensUseCase.execute(refreshToken);
        TokenPair tokens = result.tokens();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(result.user());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> resetPasswordLinkClicked(@Valid @RequestBody RequestPasswordResetDto body) {
        requestPasswordResetUseCase.execute(body);
        return ResponseEntity.ok("We sent you a password reset link.");
    }
}
