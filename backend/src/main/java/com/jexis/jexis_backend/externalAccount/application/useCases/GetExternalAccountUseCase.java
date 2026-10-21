package com.jexis.jexis_backend.externalAccount.application.useCases;

import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.externalAccount.domain.exceptions.ExternalAccountNotFoundException;
import com.jexis.jexis_backend.externalAccount.infrastructure.ExternalAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetExternalAccountUseCase {
    private final ExternalAccountRepository repo;

    public ExternalAccount execute(UUID externalAccountId) {
        return repo.findByIdAndIsDeletedFalse(externalAccountId).orElseThrow(ExternalAccountNotFoundException::new);
    }
}
