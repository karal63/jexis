package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.user.application.dto.ConfirmPasswordResetDto;
import com.jexis.jexis_backend.passwordResetToken.application.useCases.GetPasswordResetTokenByTokenUseCase;
import com.jexis.jexis_backend.passwordResetToken.application.useCases.UsePasswordResetTokenUseCase;
import com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmPasswordResetUseCase {
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UsePasswordResetTokenUseCase usePasswordResetTokenUseCase;
    private final GetPasswordResetTokenByTokenUseCase getPasswordResetTokenByTokenUseCase;

    @Transactional
    public void execute(ConfirmPasswordResetDto body) {
        PasswordResetToken passwordResetToken = getPasswordResetTokenByTokenUseCase.execute(body.token());

        // Claim the token before changing credentials; all writes roll back together.
        usePasswordResetTokenUseCase.execute(passwordResetToken.getId());
        changePasswordUseCase.execute(passwordResetToken.getUser().getId(), body.password());
    }
}
