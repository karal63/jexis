package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.RetrieveAccountByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.stripe.model.Address;

/**
 * EditAccountUseCase
 * This service class implements the use case for editing an existing account.
 * It contains the business logic related to updating an account, such as
 * interacting with the repository to fetch and save the updated data, and
 * validating if user with given id exists.
 * Author: Leo
 */
@Service
@RequiredArgsConstructor
public class EditAccountUseCase {
    private final AccountRepository accountRepo;
    private final RetrieveAccountByIdUseCase retrieveAccountByIdUseCase;
    private final GetAccountByStripeIdUseCase getAccountByStripeIdUseCase;

    /**
     * Handles account editing.
     * Accepts a {@link EditAccountDto} payload and {@param id} from controller,
     * looks for
     * the account, calls the repository to update the account, and returns
     * the updated {@link Account}.
     *
     * @param accountId the ID of the account to edit
     * @return the updated account
     */
    public void execute(String accountId) {
        com.stripe.model.Account stripeAccount = retrieveAccountByIdUseCase.execute(accountId);
        Account dbAccount = getAccountByStripeIdUseCase.execute(stripeAccount.getId());

        String businessType = stripeAccount.getBusinessType();
        Address address;
        if ("individual".equals(businessType)) {
            dbAccount.setFirstName(stripeAccount.getIndividual().getFirstName());
            dbAccount.setLastName(stripeAccount.getIndividual().getLastName());
            address = stripeAccount.getIndividual().getAddress();
            dbAccount.setPhone(stripeAccount.getIndividual().getPhone());
        } else if ("company".equals(businessType)
                || "non_profit".equals(businessType)
                || "government_entity".equals(businessType)) {
            dbAccount.setFirstName(stripeAccount.getCompany().getName());
            dbAccount.setLastName(stripeAccount.getBusinessProfile().getName());
            address = stripeAccount.getCompany().getAddress();
            dbAccount.setPhone(stripeAccount.getCompany().getPhone());
        } else {
            return;
        }
        dbAccount.setCity(address.getCity());
        dbAccount.setCountry(address.getCountry());
        dbAccount.setLine1(address.getLine1());
        dbAccount.setLine2(address.getLine2());
        dbAccount.setPostalCode(address.getPostalCode());
        dbAccount.setState(address.getState());
        dbAccount.setEmail(stripeAccount.getEmail());

        accountRepo.save(dbAccount);
    }
}
