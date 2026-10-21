package com.jexis.jexis_backend.user.application.useCases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotFoundException;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;

/**
 * GetUsersUseCase
 *
 * This service class implements the use case for retrieving all existing
 * users. It contains only the business logic related to fetching users,
 * such as interacting with the repository to fetch the data.
 *
 * Author: Leo
 */
@Service
public class GetUserUseCase {

    UserRepository repo;

    public GetUserUseCase(UserRepository userRepository) {
        this.repo = userRepository;
    }

    /**
     * Handles fetching all users.
     * Calls the repository to fetch all users and returns the list of
     * users.
     *
     * @return found user
     */
    public User execute(UUID id) {
        return repo.findByIdAndIsDeletedFalse(id).orElseThrow(UserNotFoundException::new);
    }
}
