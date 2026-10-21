package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotFoundException;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserByActivationTokenHashUseCase {
    private final UserRepository userRepository;

    public User execute(String tokenHash) {
        return userRepository.findByActivationTokenHash(tokenHash)
                .orElseThrow(UserNotFoundException::new);
    }
}
