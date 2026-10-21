package com.jexis.jexis_backend.stripe.application.useCases;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.stripe.application.dto.CreateStripeHolderDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.issuing.Cardholder;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.CardholderCreateParams;

import jakarta.servlet.http.HttpServletRequest;

/**
 * CreateCardHolderUseCase
 *
 * This service class implements the use case for creating a new cardholder.
 * It contains only the business logic related to cardholder creation, such as
 * validating input data and interacting with the repository to persist the new
 * cardholder.
 *
 * Author: Leo
 */
@Service
public class CreateStripeHolderUseCase {
    private final StripeClient stripe;
    private final AsyncLogger logger;

    public CreateStripeHolderUseCase(StripeClient stripe, AsyncLogger logger) {
        this.stripe = stripe;
        this.logger = logger;
    }

    /**
     * Creates a new cardholder
     *
     * Accepts connected account id from controller, creates a new
     * cardholder, and returns the created cardholder.
     *
     * @param connectedAccountId the id of the connected account
     * 
     * @return the created cardholder
     */
    public Cardholder execute(HttpServletRequest request, CreateStripeHolderDto dto) {
        try {
            logger.info("STRIPE", "Starting cardholder creation for account: " + dto.connectedAccountId());

            String ip = extractClientIp(request);
            long acceptanceTime = Instant.now().getEpochSecond();

            CardholderCreateParams params = CardholderCreateParams.builder()
                    .setType(CardholderCreateParams.Type.INDIVIDUAL)
                    .setName(dto.firstName())
                    .setEmail(dto.email())
                    .setPhoneNumber(dto.phoneNumber())
                    .setBilling(
                            CardholderCreateParams.Billing.builder()
                                    .setAddress(
                                            CardholderCreateParams.Billing.Address
                                                    .builder()
                                                    .setLine1(dto.addressLine1())
                                                    .setCity(dto.city())
                                                    .setState(dto.state())
                                                    .setCountry(dto.country())
                                                    .setPostalCode(dto
                                                            .postalCode())
                                                    .build())
                                    .build())
                    .setIndividual(
                            CardholderCreateParams.Individual.builder()
                                    .setFirstName(dto.firstName())
                                    .setLastName(dto.lastName())
                                    .setCardIssuing(
                                            CardholderCreateParams.Individual.CardIssuing.builder()
                                                    .setUserTermsAcceptance(
                                                            CardholderCreateParams.Individual.CardIssuing.UserTermsAcceptance
                                                                    .builder()
                                                                    .setIp(ip)
                                                                    .setDate(acceptanceTime)
                                                                    .build())
                                                    .build())
                                    .build())
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setStripeAccount(dto.connectedAccountId())
                    .build();

            Cardholder cardholder = stripe.v1().issuing().cardholders().create(params, options);
            logger.info("STRIPE", "Cardholder created successfully with id: " + cardholder.getId());

            return cardholder;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isEmpty()) {
            // first IP is original client
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
