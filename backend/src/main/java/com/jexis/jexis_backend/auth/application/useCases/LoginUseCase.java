package com.jexis.jexis_backend.auth.application.useCases;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.auth.application.dto.LoginDto;
import com.jexis.jexis_backend.auth.application.dto.LoginResult;
import com.jexis.jexis_backend.auth.application.dto.TokenPair;
import com.jexis.jexis_backend.auth.domain.exception.WrongEmailOrPasswordException;
import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;

/**
 * LoginUseCase
 *
 * This service class implements the use case for logging in a user.
 * It contains only the business logic related to user authentication, such as
 * validating input data and interacting with the repository to verify user
 * credentials.
 *
 * Author: Leo
 */
@Service
public class LoginUseCase {
    private final AuthSessionService sessions;
    private final UserRepository userRepo;
    private final AsyncLogger logger;
    private final Argon2PasswordEncoder argon = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);

    public LoginUseCase(
            AuthSessionService sessions, UserRepository userRepo, AsyncLogger logger) {
        this.sessions = sessions;
        this.userRepo = userRepo;
        this.logger = logger;
    }

    /**
     * Logs user in
     *
     * Accepts a {@link LoginDto} payload from controller, looks for
     * an account, validates credentials, generates tokens, and returns
     * {@link Account} with tokens.
     *
     * @param body the login data transfer object containing email and password
     * 
     * @return a {@link LoginResult} containing the authenticated user and token
     *         pair
     */
    @Transactional
    public LoginResult execute(LoginDto body) {
        logger.info("AUTH", "Login attempt for email: " + body.email());

        User user = userRepo.findForLogin(body.email()).orElseThrow(() -> new WrongEmailOrPasswordException());

        if (!argon.matches(body.password(), user.getPassword())) {
            logger.info("AUTH", "Login failed: invalid password for email " + body.email());
            throw new WrongEmailOrPasswordException();
        }

        TokenPair tokens = sessions.create(user.getId());

        UserResponseDto userResponse = new UserResponseDto(user.getId(), user.getFirstName(),
                user.getLastName(), user.getEmail(),
                user.getPhoneNumber(), user.getRoles(), user.getIsActivated(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        logger.info("AUTH", "Login succeeded for user: " + user.getEmail());
        return new LoginResult(userResponse, tokens);
    }
}
