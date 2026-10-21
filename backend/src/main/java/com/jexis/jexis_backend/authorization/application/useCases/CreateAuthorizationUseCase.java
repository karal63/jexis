package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.authorization.application.dto.CreateAuthorizationDto;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import com.jexis.jexis_backend.card.application.useCases.GetCardByStripeIdUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletByFAIdUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import org.springframework.stereotype.Service;

@Service
public class CreateAuthorizationUseCase {
    private final GetCardByStripeIdUseCase getCardByStripeIdUseCase;
    private final GetWalletByFAIdUseCase getWalletByFAIdUseCase;
    private final AuthorizationRepository authorizationRepository;
    private final SyncBalanceUseCase syncBalanceUseCase;

    public CreateAuthorizationUseCase(
            GetCardByStripeIdUseCase getCardByStripeIdUseCase,
            GetWalletByFAIdUseCase getWalletByFAIdUseCase,
            AuthorizationRepository authorizationRepository, SyncBalanceUseCase syncBalanceUseCase) {
        this.getCardByStripeIdUseCase = getCardByStripeIdUseCase;
        this.getWalletByFAIdUseCase = getWalletByFAIdUseCase;
        this.authorizationRepository = authorizationRepository;
        this.syncBalanceUseCase = syncBalanceUseCase;
    }

    public void execute(CreateAuthorizationDto dto) {
        Card card = getCardByStripeIdUseCase.execute(dto.cardId());
        Wallet wallet = getWalletByFAIdUseCase.execute(dto.walletId());
        syncBalanceUseCase.execute(wallet.getAccount().getConnectAccountId(), dto.walletId());

        Authorization authorization = new Authorization(
                dto.stripeAuthorizationId(),
                wallet,
                card,
                dto.approved(),
                dto.amount(),
                dto.currency(),
                dto.status()
        );

        authorization.setMerchantName(dto.merchantName());
        authorization.setMerchantCategory(dto.merchantCategory());
        authorization.setMerchantCity(dto.merchantCity());
        authorization.setMerchantCountry(dto.merchantCountry());

        authorizationRepository.save(authorization);
    }
}
