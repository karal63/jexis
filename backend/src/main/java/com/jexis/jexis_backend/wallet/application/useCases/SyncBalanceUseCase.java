package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.RetrieveFinancialAccountUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import com.stripe.model.treasury.FinancialAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.wallet.domain.WalletCurrency;

@Service
@RequiredArgsConstructor
public class SyncBalanceUseCase {
    private final RetrieveFinancialAccountUseCase retrieveFinancialAccountUseCase;
    private final WalletRepository repo;
    private final GetWalletByFAIdUseCase getWalletByFAIdUseCase;

    public void execute(String accountId, String financialAccountId) {
        FinancialAccount financialAccount = retrieveFinancialAccountUseCase.execute(accountId, financialAccountId);

        Wallet wallet = getWalletByFAIdUseCase.execute(financialAccount.getId());

        String currency = WalletCurrency.toStripe(wallet.getCurrency());
        Long balance = financialAccount.getBalance() == null || financialAccount.getBalance().getCash() == null
                ? null : financialAccount.getBalance().getCash().get(currency);
        if (balance == null) {
            throw new IllegalStateException("Stripe financial account is missing its " + currency + " cash balance");
        }
        wallet.setAvailableBalance(balance);
        repo.save(wallet);
    }
}
