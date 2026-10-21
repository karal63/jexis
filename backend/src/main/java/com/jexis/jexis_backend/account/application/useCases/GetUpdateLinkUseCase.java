package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.stripe.application.useCases.CreateLinkUseCase;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetUpdateLinkUseCase {
    private final CreateLinkUseCase createLinkUseCase;
    private final GetAccountUseCase getAccountUseCase;

    public GetUpdateLinkUseCase(CreateLinkUseCase createLinkUseCase, GetAccountUseCase getAccountUseCase) {
        this.createLinkUseCase = createLinkUseCase;
        this.getAccountUseCase = getAccountUseCase;
    }

    public AccountLink execute(UUID accountId) {
        Account account = getAccountUseCase.execute(accountId);
        return createLinkUseCase.execute(account.getConnectAccountId(), AccountLinkCreateParams.Type.ACCOUNT_UPDATE);
    }
}
