package com.jexis.jexis_backend.passwordResetToken.application.useCases;

import com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken;
import com.jexis.jexis_backend.passwordResetToken.domain.exceptions.PasswordResetTokenNotFoundException;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPasswordResetTokenUseCase {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken execute(UUID userId) {
        return passwordResetTokenRepository.findById(userId)
                .orElseThrow(PasswordResetTokenNotFoundException::new);
    }
}
