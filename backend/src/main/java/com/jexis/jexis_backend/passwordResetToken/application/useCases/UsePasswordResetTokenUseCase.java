package com.jexis.jexis_backend.passwordResetToken.application.useCases;

import com.jexis.jexis_backend.auth.domain.exception.InvalidPasswordResetTokenException;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsePasswordResetTokenUseCase {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void execute(UUID tokenId) {
        if (passwordResetTokenRepository.consume(tokenId, LocalDateTime.now()) != 1) {
            throw new InvalidPasswordResetTokenException();
        }
    }
}
