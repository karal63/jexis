package com.jexis.jexis_backend.passwordResetToken.application.useCases;

import com.jexis.jexis_backend.auth.domain.exception.InvalidPasswordResetTokenException;
import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class GetPasswordResetTokenByTokenUseCase {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken execute(String token) {
        String hashedToken = HashUtils.sha256(token);

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (passwordResetToken.getUsedAt() != null || passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPasswordResetTokenException();
        }

        return passwordResetToken;
    }
}
