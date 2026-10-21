package com.jexis.jexis_backend.passwordResetToken.application.useCases;

import com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsePasswordResetTokenUseCase {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final GetPasswordResetTokenUseCase getPasswordResetTokenUseCase;

    public void execute(UUID tokenId) {
        PasswordResetToken passwordResetToken = getPasswordResetTokenUseCase.execute(tokenId);
        passwordResetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(passwordResetToken);
    }
}
