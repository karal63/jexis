package com.jexis.jexis_backend.stripe.application.useCases;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.common.web.error.DomainException;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.param.AccountCreateParams;

/**
 * CreateConnectUseCase
 *
 * This service class implements the use case for creating a new connect
 * account.
 * It contains only the business logic related to account creation
 *
 * Author: Leo
 */
@Service
public class CreateConnectUseCase {
    private final StripeClient stripe;
    private final AsyncLogger logger;

    public CreateConnectUseCase(StripeClient stripe, AsyncLogger logger) {
        this.stripe = stripe;
        this.logger = logger;
    }

    /**
     * Creates a new connect account
     *
     * Accepts account email from controller, creates a new connect account, and
     * returns the created account.
     *
     * @param email the email of the account to create
     *
     * @return the created account
     */
    public Account execute(String email) {
        try {
            logger.info("STRIPE", "Starting connect account creation for email: " +
                    email);

            AccountCreateParams params = AccountCreateParams.builder()
                    .setCountry("US")
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setTransfers(
                                            AccountCreateParams.Capabilities.Transfers
                                                    .builder()
                                                    .setRequested(true)
                                                    .build())
                                    .setCardIssuing(
                                            AccountCreateParams.Capabilities.CardIssuing
                                                    .builder()
                                                    .setRequested(true)
                                                    .build())
                                    .setTreasury(
                                            AccountCreateParams.Capabilities.Treasury
                                                    .builder()
                                                    .setRequested(true)
                                                    .build())
                                    .build())
                    .setController(
                            AccountCreateParams.Controller.builder()
                                    .setStripeDashboard(
                                            AccountCreateParams.Controller.StripeDashboard
                                                    .builder()
                                                    .setType(AccountCreateParams.Controller.StripeDashboard.Type.NONE)
                                                    .build())
                                    .setFees(
                                            AccountCreateParams.Controller.Fees
                                                    .builder()
                                                    .setPayer(AccountCreateParams.Controller.Fees.Payer.APPLICATION)
                                                    .build())
                                    .setLosses(
                                            AccountCreateParams.Controller.Losses
                                                    .builder()
                                                    .setPayments(
                                                            AccountCreateParams.Controller.Losses.Payments.APPLICATION)
                                                    .build())
                                    .setRequirementCollection(
                                            AccountCreateParams.Controller.RequirementCollection.APPLICATION)
                                    .build())
                    .build();

            Account account = stripe.v1().accounts().create(params);
            logger.info("STRIPE", "Connect account created successfully with id: " +
                    account.getId());

            return account;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
