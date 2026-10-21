package com.jexis.jexis_backend.auth.application.useCases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.auth.application.dto.SignupResult;
import com.jexis.jexis_backend.auth.application.dto.TokenPair;
import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;
import com.jexis.jexis_backend.user.application.dto.CreateDto;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;
import com.jexis.jexis_backend.user.application.useCases.CreateUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.enums.UserRole;

/**
 * SignupUseCase
 *
 * This service class implements the use case for signing up a new user.
 * It contains only the business logic related to user registration, such as
 * validating input data and interacting with the repository to persist the new
 * user.
 *
 * Author: Leo
 */
@Service
public class SignupUseCase {
    JwtUtil jwtUtil;
    CreateUserUseCase createUserUseCase;
    private final AsyncLogger logger;

    public SignupUseCase(JwtUtil jwtUtil, CreateUserUseCase createUserUseCase, AsyncLogger logger) {
        this.jwtUtil = jwtUtil;
        this.createUserUseCase = createUserUseCase;
        this.logger = logger;
    }

    /*
     * Signs up a new user
     *
     * Accepts a {@link CreateDto} payload from controller, creates a new user,
     * generates tokens, and returns {@link SignupResult} with the new user and
     * tokens.
     *
     * @param body the signup data transfer object containing email and password
     * 
     * @return a {@link SignupResult} containing the new user and token
     * pair
     */
    public SignupResult execute(CreateDto body) {
        logger.info("AUTH", "Signup started for email: " + body.getEmail());

        List<UserRole> roles = List.of(UserRole.USER);
        User user = createUserUseCase.execute(body, roles);

        TokenPair tokens = jwtUtil.generateTokens(
                user.getId(), user.getFirstName(), user.getEmail(), user.getIsActivated(), user.getRoles());

        UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getFirstName(),
                user.getLastName(), user.getEmail(),
                user.getPhoneNumber(), user.getRoles(), user.getIsActivated(), user.getCreatedAt(),
                user.getUpdatedAt());

        logger.info("AUTH", "Signup completed for user: " + user.getEmail());
        return new SignupResult(userResponse, tokens);
    }
}
