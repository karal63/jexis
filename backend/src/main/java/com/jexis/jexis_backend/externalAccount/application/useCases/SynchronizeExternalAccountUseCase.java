package com.jexis.jexis_backend.externalAccount.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.GetAccountByStripeIdUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.externalAccount.infrastructure.ExternalAccountRepository;
import com.stripe.model.BankAccount;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SynchronizeExternalAccountUseCase {
    private final ExternalAccountRepository repo;
    private final AsyncLogger logger;
    private final GetAccountByStripeIdUseCase getAccountByStripeIdUseCase;
    private final EntityManager entityManager;

    public void execute(BankAccount stripeExternalAccount) {
        entityManager.createNativeQuery(
                        "SELECT pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, stripeExternalAccount.getId())
                .getSingleResult();

        Optional<ExternalAccount> externalAccount = repo.findByStripeExternalAccountIdAndIsDeletedFalse(stripeExternalAccount.getId());

        if (externalAccount.isPresent()) {
            externalAccount.get().setBankName(stripeExternalAccount.getBankName());
            externalAccount.get().setLast4(stripeExternalAccount.getLast4());
            externalAccount.get().setCountry(stripeExternalAccount.getCountry());
            externalAccount.get().setCurrency(stripeExternalAccount.getCurrency());
            externalAccount.get().setDefault(stripeExternalAccount.getDefaultForCurrency());

            repo.save(externalAccount.get());
            logger.info("INFO", "External account updated");
        } else {
            Account account = getAccountByStripeIdUseCase.execute(stripeExternalAccount.getAccount());

            ExternalAccount newAccount = new ExternalAccount(
                    account,
                    stripeExternalAccount.getId(),
                    stripeExternalAccount.getBankName(),
                    stripeExternalAccount.getLast4(),
                    stripeExternalAccount.getCountry(),
                    stripeExternalAccount.getCurrency(),
                    stripeExternalAccount.getDefaultForCurrency()
            );

            repo.save(newAccount);
            logger.info("INFO", "External account created");
        }

    }
}
