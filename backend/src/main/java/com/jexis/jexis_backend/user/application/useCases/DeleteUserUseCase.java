package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {
    private final UserRepository repo;
    private final GetUserUseCase getUserUseCase;
    private final AuthSessionService sessions;
    private final MemberRepository members;

    @Transactional
    public void execute(UUID id) {
        sessions.revokeAll(id);
        User user = getUserUseCase.execute(id);
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        members.deleteByUserId(id);
        repo.save(user);
    }
}
