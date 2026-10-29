package com.jexis.jexis_backend.passwordResetToken.application.useCases;

import com.jexis.jexis_backend.common.ratelimit.RateLimitService;

import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer;
import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailTask;
import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import com.jexis.jexis_backend.user.application.useCases.GetUserByEmailUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreatePasswordResetTokenUseCase {
    @Value("${application.origin}")
    private String applicationOrigin;

    private final SecureRandom RANDOM = new SecureRandom();
    private final Argon2PasswordEncoder argon = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
    private final PasswordResetTokenRepository repo;
    private final GetUserByEmailUseCase getUserByEmailUseCase;
    private final EmailProducer emailProducer;
    private final RateLimitService rateLimits;

    @Transactional
    public void execute(String email) {
        if (!rateLimits.allowResetEmail(email)) return;
        Optional<User> user = getUserByEmailUseCase.execute(email);

        if (user.isPresent()) {
            byte[] bytes = new byte[32]; // 256 bits
            RANDOM.nextBytes(bytes);
            String token = HexFormat.of().formatHex(bytes);

            String hashedToken = HashUtils.sha256(token);

            // store token
            PasswordResetToken passwordResetToken = new PasswordResetToken(
                    user.get(),
                    hashedToken,
                    LocalDateTime.now().plusMinutes(30),
                    LocalDateTime.now()
            );

            // save token
            repo.save(passwordResetToken);

            // send email task to RabbitMQ
            emailProducer.sendEmailTask(new EmailTask(
                    user.get().getEmail(),
                    "Reset your password",
                    """
                    Hi %s,

                    We received a request to reset your password.

                    Click this link below to reset your password:

                    %s/reset-password?token=%s

                    This link expires in 30 minutes.

                    If you didn't request this, ignore this email.""".formatted(user.get().getFirstName(), applicationOrigin.replaceAll("/+$", ""), token)
            ));
        }

    }
}
