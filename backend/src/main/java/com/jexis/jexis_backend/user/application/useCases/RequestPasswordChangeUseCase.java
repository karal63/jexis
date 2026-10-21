package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.passwordResetToken.application.useCases.CreatePasswordResetTokenUseCase;
import com.jexis.jexis_backend.user.application.dto.RequestPasswordChangeDto;
import org.springframework.stereotype.Service;

@Service
public class RequestPasswordChangeUseCase {
    private final CreatePasswordResetTokenUseCase createPasswordResetTokenUseCase;

    public RequestPasswordChangeUseCase(CreatePasswordResetTokenUseCase createPasswordResetTokenUseCase) {
        this.createPasswordResetTokenUseCase = createPasswordResetTokenUseCase;
    }

    public void execute(RequestPasswordChangeDto body) {
        createPasswordResetTokenUseCase.execute(body.email());
    }
}
