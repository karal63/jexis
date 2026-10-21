package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.exceptions.AuthorizationNotFoundException;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetAuthorizationUseCase {
    private final AuthorizationRepository authorizationRepository;

    public GetAuthorizationUseCase(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    public Authorization execute(UUID id) {
        return authorizationRepository.findById(id)
                .orElseThrow(AuthorizationNotFoundException::new);
    }
}
