package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.authorization.application.dto.UpdateAuthorizationDto;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import org.springframework.stereotype.Service;

@Service
public class UpdateAuthorizationUseCase {
    private final AuthorizationRepository authorizationRepository;
    private final GetAuthorizationByStripeIdUseCase getAuthorizationByStripeIdUseCase;
    private final SyncBalanceUseCase syncBalanceUseCase;

    public UpdateAuthorizationUseCase(AuthorizationRepository authorizationRepository, GetAuthorizationByStripeIdUseCase getAuthorizationByStripeIdUseCase, SyncBalanceUseCase syncBalanceUseCase) {
        this.authorizationRepository = authorizationRepository;
        this.getAuthorizationByStripeIdUseCase = getAuthorizationByStripeIdUseCase;
        this.syncBalanceUseCase = syncBalanceUseCase;
    }

    public void execute(UpdateAuthorizationDto dto) {
        Authorization authorization = getAuthorizationByStripeIdUseCase.execute(dto.stripeAuthorizationId());

        boolean hasChanges = false;

        if (!authorization.getApproved().equals(dto.approved())) {
            authorization.setApproved(dto.approved());
            hasChanges = true;
        }

        if (!authorization.getStatus().equals(dto.status())) {
            authorization.setStatus(dto.status());
            hasChanges = true;
        }

        if (!authorization.getAmount().equals(dto.amount())) {
            authorization.setAmount(dto.amount());
            hasChanges = true;
        }

        if ((authorization.getMerchantName() == null && dto.merchantName() != null) ||
                (authorization.getMerchantName() != null && !authorization.getMerchantName().equals(dto.merchantName()))) {
            authorization.setMerchantName(dto.merchantName());
            hasChanges = true;
        }

        if ((authorization.getMerchantCategory() == null && dto.merchantCategory() != null) ||
                (authorization.getMerchantCategory() != null && !authorization.getMerchantCategory().equals(dto.merchantCategory()))) {
            authorization.setMerchantCategory(dto.merchantCategory());
            hasChanges = true;
        }

        if ((authorization.getMerchantCity() == null && dto.merchantCity() != null) ||
                (authorization.getMerchantCity() != null && !authorization.getMerchantCity().equals(dto.merchantCity()))) {
            authorization.setMerchantCity(dto.merchantCity());
            hasChanges = true;
        }

        if ((authorization.getMerchantCountry() == null && dto.merchantCountry() != null) ||
                (authorization.getMerchantCountry() != null && !authorization.getMerchantCountry().equals(dto.merchantCountry()))) {
            authorization.setMerchantCountry(dto.merchantCountry());
            hasChanges = true;
        }

        if (hasChanges) {
            syncBalanceUseCase.execute(authorization.getWallet().getAccount().getConnectAccountId(), authorization.getWallet().getStripeFinancialAccountId());
            authorizationRepository.save(authorization);
        }
    }
}
