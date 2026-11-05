package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer;
import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailTask;
import com.jexis.jexis_backend.emailService.application.EmailMessages;
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
                EmailMessages.activationSubject(),
                EmailMessages.activationBody(user.getFirstName(),
                        publicApiUrl.replaceAll("/+$", "") + "/user/activate?token=" + rawToken)
        ));

    }
}
