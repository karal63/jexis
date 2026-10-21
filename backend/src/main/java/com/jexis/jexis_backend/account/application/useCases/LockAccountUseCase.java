package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.exception.AccountNotFoundException;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LockAccountUseCase {
    private final AccountRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Account execute(UUID accountId) {
        return repository.findForResourceUpdate(accountId).orElseThrow(AccountNotFoundException::new);
    }
}
