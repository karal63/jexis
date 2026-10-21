package com.jexis.jexis_backend.stripe.application.useCases;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.wallet.application.dto.EditWalletDto;
import com.stripe.StripeClient;
import com.stripe.model.treasury.FinancialAccount;
import com.stripe.net.RequestOptions;
import com.stripe.param.treasury.FinancialAccountUpdateParams;

@Service
public class UpdateStripeWalletUseCase {
    private final StripeClient client;

    public UpdateStripeWalletUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId, String walletId, EditWalletDto dto) {
        try {
            RequestOptions requestParam = RequestOptions.builder().setStripeAccount(accountId).build();
            FinancialAccount resource = client.v1().treasury().financialAccounts().retrieve(walletId, requestParam);

            FinancialAccountUpdateParams.Builder builder = FinancialAccountUpdateParams.builder();

            if (dto.getName() != null) {
                builder.setNickname(dto.getName()).build();
            }

            resource.update(builder.build(), requestParam);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}
