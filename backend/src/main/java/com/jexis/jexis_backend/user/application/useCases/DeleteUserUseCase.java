package com.jexis.jexis_backend.user.application.useCases;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotFoundException;
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
public class DeleteUserUseCase {
    UserRepository repo;
    private final GetUserUseCase getUserUseCase;

    public DeleteUserUseCase(UserRepository repo, GetUserUseCase getUserUseCase) {
        this.repo = repo;
        this.getUserUseCase = getUserUseCase;
    }

    /*
     * Deletes a user
     *
     * Accepts a UUID of the user to delete and removes the user from the
     * repository.
     *
     * @param id the UUID of the user to delete
     * 
     */
    public void execute(UUID id) {
        User user = getUserUseCase.execute(id);

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        repo.save(user);
    }
}