package com.jexis.jexis_backend.config;

import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer;
import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailTask;
import com.jexis.jexis_backend.passwordResetToken.application.useCases.CreatePasswordResetTokenUseCase;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import com.jexis.jexis_backend.user.application.useCases.GetUserByEmailUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.application.useCases.SendActivationLinkUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailEnvironmentTest {
    private User user() {
        var user = new User();
        user.setFirstName("Test");
        user.setEmail("test@example.com");
        return user;
    }

    @Test void activationUsesPublicApiUrl() {
        var producer = mock(EmailProducer.class);
        var service = new SendActivationLinkUseCase(producer, mock(GetUserUseCase.class));
        ReflectionTestUtils.setField(service, "publicApiUrl", "https://api.example.com/api/");
        service.execute(user(), "test-token");
        var email = ArgumentCaptor.forClass(EmailTask.class);
        verify(producer).sendEmailTask(email.capture());
        assertThat(email.getValue().getBody()).contains("https://api.example.com/api/user/activate?token=test-token")
                .doesNotContain("localhost");
    }

    @Test void passwordResetUsesFrontendOrigin() {
        var producer = mock(EmailProducer.class);
        var lookup = mock(GetUserByEmailUseCase.class);
        when(lookup.execute("test@example.com")).thenReturn(Optional.of(user()));
        var service = new CreatePasswordResetTokenUseCase(mock(PasswordResetTokenRepository.class), lookup, producer);
        ReflectionTestUtils.setField(service, "applicationOrigin", "https://app.example.com/");
        service.execute("test@example.com");
        var email = ArgumentCaptor.forClass(EmailTask.class);
        verify(producer).sendEmailTask(email.capture());
        assertThat(email.getValue().getBody()).contains("https://app.example.com/reset-password?token=")
                .doesNotContain("localhost");
    }
}
