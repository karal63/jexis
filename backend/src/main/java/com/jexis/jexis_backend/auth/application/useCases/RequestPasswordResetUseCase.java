package com.jexis.jexis_backend.auth.application.useCases;

import com.jexis.jexis_backend.auth.application.dto.RequestPasswordResetDto;
import com.jexis.jexis_backend.passwordResetToken.application.useCases.CreatePasswordResetTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestPasswordResetUseCase {
    private final CreatePasswordResetTokenUseCase createPasswordResetTokenUseCase;

    public void execute(RequestPasswordResetDto body) {
        createPasswordResetTokenUseCase.execute(body.email());
    }
}
