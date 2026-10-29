package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {
    private final UserRepository repo;
    private final AuthSessionService sessions;
    private final GetUserUseCase getUserUseCase;
    private final Argon2PasswordEncoder argon = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);

    @Transactional
    public User execute(UUID id, String password) {
        sessions.revokeAll(id);
        User user = getUserUseCase.execute(id);
        user.setPassword(argon.encode(password));
        return repo.save(user);
    }
}
