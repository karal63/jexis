package com.jexis.jexis_backend.user.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.user.application.dto.EditDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;

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
@Service
public class EditUserUseCase {
    UserRepository repo;
    private final GetUserUseCase getUserUseCase;

    public EditUserUseCase(UserRepository repo, GetUserUseCase getUserUseCase) {
        this.repo = repo;
        this.getUserUseCase = getUserUseCase;
    }

    /**
     * Edits an existing user
     * Accepts a UUID of the user to edit and an {@link EditDto} payload from
     * controller,
     * updates the user, and returns the updated user.
     *
     * @param id   the UUID of the user to edit
     * @param body the edit data transfer object containing updated user information
     * @return a {@link User} containing the updated user
     */
    public User execute(UUID id, EditDto body) {
        User user = getUserUseCase.execute(id);

        if (body.getFirstName() != null) {
            user.setFirstName(body.getFirstName());
        }
        if (body.getLastName() != null) {
            user.setLastName(body.getLastName());
        }
        if (body.getPhoneNumber() != null) {
            user.setPhoneNumber(body.getPhoneNumber());
        }
        return repo.save(user);
    }
}