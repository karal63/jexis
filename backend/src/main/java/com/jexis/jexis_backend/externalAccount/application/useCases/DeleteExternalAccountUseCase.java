package com.jexis.jexis_backend.externalAccount.application.useCases;

import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.externalAccount.infrastructure.ExternalAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteExternalAccountUseCase {
    private final ExternalAccountRepository repo;
    private final GetExternalAccountByStripeIdUseCase getExternalAccountByStripeIdUseCase;

    public void execute(String accountId) {
        ExternalAccount externalAccount = getExternalAccountByStripeIdUseCase.execute(accountId);
        externalAccount.setIsDeleted(true);
        externalAccount.setDeletedAt(LocalDateTime.now());
        repo.save(externalAccount);
    }
}
