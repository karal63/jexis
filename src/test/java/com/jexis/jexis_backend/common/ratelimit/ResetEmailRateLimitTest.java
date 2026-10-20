package com.jexis.jexis_backend.common.ratelimit;

import com.jexis.jexis_backend.auth.application.dto.RequestPasswordResetDto;
import com.jexis.jexis_backend.auth.application.useCases.RequestPasswordResetUseCase;
import com.jexis.jexis_backend.user.application.dto.RequestPasswordChangeDto;
import com.jexis.jexis_backend.user.application.useCases.RequestPasswordChangeUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserByEmailUseCase;
import com.jexis.jexis_backend.passwordResetToken.application.useCases.CreatePasswordResetTokenUseCase;
import com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository;
import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class ResetEmailRateLimitTest {
    @Test void bothEntryPointsSuppressDeliveryAndDatabaseWorkWhenRecipientLimited() {
        var limits = mock(RateLimitService.class);
        var repo = mock(PasswordResetTokenRepository.class);
        var lookup = mock(GetUserByEmailUseCase.class);
        var producer = mock(EmailProducer.class);
        var reset = new CreatePasswordResetTokenUseCase(repo, lookup, producer, limits);
        new RequestPasswordResetUseCase(reset).execute(new RequestPasswordResetDto("user@example.com"));
        new RequestPasswordChangeUseCase(reset).execute(new RequestPasswordChangeDto("user@example.com"));
        verify(limits, times(2)).allowResetEmail("user@example.com");
        verifyNoInteractions(repo, lookup, producer);
    }
}
