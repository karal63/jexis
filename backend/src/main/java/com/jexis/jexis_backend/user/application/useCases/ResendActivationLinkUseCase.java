package com.jexis.jexis_backend.user.application.useCases;

import java.util.UUID;

import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import com.jexis.jexis_backend.common.ratelimit.RateLimitService;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.exceptions.UserAlreadyActivatedException;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotFoundException;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResendActivationLinkUseCase {
    private final UserRepository userRepository;
    private final SendActivationLinkUseCase sendActivationLinkUseCase;
    private final RateLimitService rateLimits;

    @Transactional
    public void execute(UUID userId) {
        User user = userRepository.findForSessionUpdate(userId)
                .orElseThrow(UserNotFoundException::new);
        if (Boolean.TRUE.equals(user.getIsActivated())) {
            throw new UserAlreadyActivatedException();
        }
        rateLimits.check("activation-email-user", userId.toString());

        String token = SendActivationLinkUseCase.generateToken();
        user.setActivationTokenHash(HashUtils.sha256(token));
        sendActivationLinkUseCase.execute(user, token);
    }
}
