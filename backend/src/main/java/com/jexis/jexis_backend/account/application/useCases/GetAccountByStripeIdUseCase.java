package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.exception.AccountNotFoundException;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAccountByStripeIdUseCase {
    private final AccountRepository repo;

    public Account execute(String stripeId) {
        return repo.findByConnectAccountIdAndIsDeletedFalse(stripeId).orElseThrow(AccountNotFoundException::new);
    }
}
