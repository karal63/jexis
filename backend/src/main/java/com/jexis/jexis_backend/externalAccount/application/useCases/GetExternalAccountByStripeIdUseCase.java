package com.jexis.jexis_backend.externalAccount.application.useCases;

import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.externalAccount.domain.exceptions.ExternalAccountNotFoundException;
import com.jexis.jexis_backend.externalAccount.infrastructure.ExternalAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetExternalAccountByStripeIdUseCase {
    private final ExternalAccountRepository repo;

    public ExternalAccount execute(String stripeId) {
        return repo.findByStripeExternalAccountIdAndIsDeletedFalse(stripeId).orElseThrow(ExternalAccountNotFoundException::new);
    }
}
