package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.externalAccount.application.useCases.GetExternalAccountUseCase;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.stripe.application.useCases.StripeOutboundTransferUseCase;
import com.jexis.jexis_backend.wallet.application.dto.CreateOutboundTransferDto;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import com.jexis.jexis_backend.wallet.domain.WalletCurrency;

@Service
@RequiredArgsConstructor
public class CreateOutboundTransferUseCase {
    private final StripeOutboundTransferUseCase stripeOutboundTransferUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final GetExternalAccountUseCase getExternalAccountUseCase;

    public void execute(UUID walletId, CreateOutboundTransferDto body) {
        Wallet wallet = getWalletUseCase.execute(walletId);
        ExternalAccount externalAccount = getExternalAccountUseCase.execute(body.externalAccountId());
        WalletCurrency.requireMatch(wallet.getCurrency(), body.currency());
        WalletCurrency.requireMatch(wallet.getCurrency(), externalAccount.getCurrency());
        stripeOutboundTransferUseCase.execute(wallet.getAccount().getConnectAccountId(), wallet.getStripeFinancialAccountId(), externalAccount.getStripeExternalAccountId(), body);
    }
}
