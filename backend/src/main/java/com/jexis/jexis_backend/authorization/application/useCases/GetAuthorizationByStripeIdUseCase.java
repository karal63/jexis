package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.exceptions.AuthorizationNotFoundException;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAuthorizationByStripeIdUseCase {
    private final AuthorizationRepository repo;

    public GetAuthorizationByStripeIdUseCase(AuthorizationRepository repo) {
        this.repo = repo;
    }

    public Authorization execute(String stripeId) {
        return repo.findByStripeAuthorizationId(stripeId).orElseThrow(AuthorizationNotFoundException::new);
    }
}
