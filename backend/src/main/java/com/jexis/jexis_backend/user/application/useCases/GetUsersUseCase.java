package com.jexis.jexis_backend.user.application.useCases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.user.domain.entities.User;
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
public class GetUsersUseCase {

    UserRepository repo;

    public GetUsersUseCase(UserRepository userRepository) {
        this.repo = userRepository;
    }

    /**
     * Handles fetching all users.
     *
     * Calls the repository to fetch all users and returns the list of
     * users.
     *
     * @return list of all users
     */
    public List<User> execute() {
        return repo.findAll();
    }

    public org.springframework.data.domain.Page<User> execute(int page, int pageSize, String search, com.jexis.jexis_backend.user.domain.enums.UserRole role, Boolean isActivated, String sortBy, String sortDirection) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page - 1), pageSize, buildSort(sortBy, sortDirection));
        return repo.findUsersWithFilters(pageable, normalize(search), role, isActivated);
    }

    private org.springframework.data.domain.Sort buildSort(String sortBy, String sortDirection) {
        org.springframework.data.domain.Sort.Direction direction = sortDirection == null || sortDirection.isBlank()
                ? org.springframework.data.domain.Sort.Direction.DESC
                : org.springframework.data.domain.Sort.Direction.fromString(sortDirection);

        String property = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "firstname" -> "firstName";
            case "lastname" -> "lastName";
            case "email" -> "email";
            case "phonenumber" -> "phoneNumber";
            default -> "createdAt";
        };

        return org.springframework.data.domain.Sort.by(direction, property);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
