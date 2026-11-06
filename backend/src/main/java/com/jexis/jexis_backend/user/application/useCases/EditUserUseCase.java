package com.jexis.jexis_backend.user.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.user.application.dto.EditDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.PhoneNumberExistsException;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import com.jexis.jexis_backend.user.infrastructure.UserWriteTransactions;
import com.jexis.jexis_backend.user.domain.ProfileInputNormalizer;

@Service
public class EditUserUseCase {
    private final UserRepository repo;
    private final UserWriteTransactions writes;
    private final GetUserUseCase getUserUseCase;

    public EditUserUseCase(UserRepository repo, GetUserUseCase getUserUseCase, UserWriteTransactions writes) {
        this.writes = writes;
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
        return writes.execute(() -> {
            User user = getUserUseCase.execute(id);

            if (body.getFirstName() != null) {
                user.setFirstName(ProfileInputNormalizer.name(body.getFirstName(), "firstName"));
            }
            if (body.getLastName() != null) {
                user.setLastName(ProfileInputNormalizer.name(body.getLastName(), "lastName"));
            }
            if (body.isPhoneNumberProvided()) {
                String phoneNumber = ProfileInputNormalizer.phone(body.getPhoneNumber());
                if (phoneNumber != null && repo.existsByPhoneNumberAndIdNot(phoneNumber, id)) {
                    throw new PhoneNumberExistsException();
                }
                user.setPhoneNumber(phoneNumber);
            }
            return repo.saveAndFlush(user);
        });
    }
}
