package com.jexis.jexis_backend.user.application.useCases;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.user.application.dto.CreateDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.enums.UserRole;
import com.jexis.jexis_backend.user.domain.exceptions.EmailExistsException;
import com.jexis.jexis_backend.user.domain.exceptions.PhoneNumberExistsException;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;

/**
 * CreateUserUseCase
 *
 * This service class implements the use case for creating a new user.
 * It contains only the business logic related to user creation, such as
 * validating input data and interacting with the repository to persist the new
 * user.
 *
 * Author: Leo
 */
@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UserRepository repo;
    private final AsyncLogger logger;
    private final Argon2PasswordEncoder argon = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
    private final SecureRandom RANDOM = new SecureRandom();
    private final SendActivationLinkUseCase sendActivationLinkUseCase;

    /*
     * Creates a new user
     *
     * Accepts a {@link CreateDto} payload from controller, creates a new user,
     * and returns {@link User} with the new user.
     *
     * @param body the create data transfer object containing email and password
     * 
     * @return a {@link User} containing the new user
     */
    public User execute(CreateDto body, List<UserRole> roles) {
        logger.info("USER", "Creating user for email: " + body.getEmail());

        Optional<User> existingUser = repo.findByEmailOrPhoneNumber(body.getEmail(), body.getPhoneNumber());

        if (existingUser.isPresent()) {
            if (existingUser.get().getEmail().equals(body.getEmail())) {
                throw new EmailExistsException();
            } else if (existingUser.get().getPhoneNumber().equals(body.getPhoneNumber())) {
                throw new PhoneNumberExistsException();
            }
        }

        byte[] bytes = new byte[32]; // 256 bits
        RANDOM.nextBytes(bytes);
        String activationToken = HexFormat.of().formatHex(bytes);
        String hashedActivationLink = HashUtils.sha256(activationToken);

        String hashedPassword = argon.encode(body.getPassword());

        User savedUser = repo
                .save(new User(body.getFirstName(), body.getLastName(), body.getEmail(), body.getPhoneNumber(),
                        hashedPassword, hashedActivationLink, roles));

        sendActivationLinkUseCase.execute(savedUser, activationToken);

        logger.info("USER", "User created successfully: " + savedUser.getEmail());
        return savedUser;
    }
}