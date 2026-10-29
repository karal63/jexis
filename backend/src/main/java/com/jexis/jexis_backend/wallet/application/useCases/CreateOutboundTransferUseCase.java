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

    private final OutboundTransferCommands commands;

    public void execute(UUID walletId, CreateOutboundTransferDto body, UUID requestKey) {
        java.util.Objects.requireNonNull(requestKey, "Idempotency key is required");
        Wallet wallet = getWalletUseCase.execute(walletId);
        ExternalAccount externalAccount = getExternalAccountUseCase.execute(body.externalAccountId());
        WalletCurrency.requireMatch(wallet.getCurrency(), body.currency());
        WalletCurrency.requireMatch(wallet.getCurrency(), externalAccount.getCurrency());
        var command = commands.prepare(wallet, externalAccount, body, requestKey);
        if (command.getStripeTransferId() != null) return;
        var frozenBody = new CreateOutboundTransferDto(command.getAmount(), command.getCurrency(),
                command.getDescription(), command.getExternalAccountId());
        String transferId = stripeOutboundTransferUseCase.execute(command.getStripeAccountId(),
                command.getFinancialAccountId(), command.getDestinationId(), frozenBody,
                "outbound-transfer-" + command.getId());
        commands.complete(command.getId(), transferId);
    }
}
