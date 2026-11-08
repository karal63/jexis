package com.jexis.jexis_backend.account.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.AccountSessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBankAccountSessionUseCase {
    private final GetAccountUseCase getAccountUseCase;
    private final StripeClient stripeClient;

    public String execute(UUID accountId, boolean manage) {
        String connectAccountId = getAccountUseCase.execute(accountId).getConnectAccountId();
        var components = AccountSessionCreateParams.Components.builder();
        if (manage) {
            var features = AccountSessionCreateParams.Components.AccountManagement.Features.builder()
                    .setExternalAccountCollection(true)
                    .build();
            components.setAccountManagement(AccountSessionCreateParams.Components.AccountManagement.builder()
                    .setEnabled(true).setFeatures(features).build());
        } else {
            var features = AccountSessionCreateParams.Components.AccountOnboarding.Features.builder()
                    .setExternalAccountCollection(true)
                    .build();
            components.setAccountOnboarding(AccountSessionCreateParams.Components.AccountOnboarding.builder()
                    .setEnabled(true).setFeatures(features).build());
        }

        var params = AccountSessionCreateParams.builder()
                .setAccount(connectAccountId)
                .setComponents(components.build())
                .build();
        try {
            return stripeClient.v1().accountSessions().create(params).getClientSecret();
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not start bank account setup", e);
        }
    }
}
