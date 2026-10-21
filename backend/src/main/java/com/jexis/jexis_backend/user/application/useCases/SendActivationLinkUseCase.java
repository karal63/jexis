package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer;
import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailTask;
import com.jexis.jexis_backend.user.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class SendActivationLinkUseCase {
    @Value("${application.public-api-url}")
    private String publicApiUrl;

    private final EmailProducer emailProducer;
    private final GetUserUseCase getUserUseCase;

    public void execute(User user, String rawToken) {
        emailProducer.sendEmailTask(new EmailTask(
                user.getEmail(),
                "Activate your account",
                """
                        Hi, %s
                        
                        Please click the following link to activate your account:
                        
                        %s/user/activate?token=%s
                        """.formatted(user.getFirstName(), publicApiUrl.replaceAll("/+$", ""), rawToken)
        ));

    }
}
