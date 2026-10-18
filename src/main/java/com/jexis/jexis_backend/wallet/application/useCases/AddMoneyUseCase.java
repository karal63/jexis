package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.stripe.application.useCases.AddReceivedCreditsUseCase;
import com.jexis.jexis_backend.wallet.application.dto.AddReceivedCreditsDto;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import org.springframework.stereotype.Service;

import java.util.UUID;
import com.jexis.jexis_backend.wallet.domain.WalletCurrency;

@Service
public class AddMoneyUseCase {
    private final AddReceivedCreditsUseCase addReceivedCreditsUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final GetAccountUseCase getAccountUseCase;

    public AddMoneyUseCase(AddReceivedCreditsUseCase addReceivedCreditsUseCase,  GetWalletUseCase getWalletUseCase,  GetAccountUseCase getAccountUseCase) {
        this.addReceivedCreditsUseCase = addReceivedCreditsUseCase;
        this.getWalletUseCase = getWalletUseCase;
        this.getAccountUseCase = getAccountUseCase;
    }

    public void execute(UUID accountId, UUID walletId, AddReceivedCreditsDto body) {
        Wallet wallet = getWalletUseCase.execute(walletId);
        Account account = getAccountUseCase.execute(accountId);

        WalletCurrency.requireMatch(wallet.getCurrency(), body.currency());

        addReceivedCreditsUseCase.execute(account.getConnectAccountId(), wallet.getStripeFinancialAccountId(), body);
    }
}
