package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserByEmailUseCase {
    private final UserRepository  userRepository;

    public Optional<User> execute(String email) {
        return userRepository.findByEmail(email);
    }
}
