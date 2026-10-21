package com.jexis.jexis_backend.authorization.presentation;

import com.jexis.jexis_backend.authorization.application.dto.CreateAuthorizationDto;
import com.jexis.jexis_backend.authorization.application.dto.UpdateAuthorizationDto;
import com.jexis.jexis_backend.authorization.application.useCases.CreateAuthorizationUseCase;
import com.jexis.jexis_backend.authorization.application.useCases.CheckAuthorizationEligibilityUseCase;
import com.jexis.jexis_backend.authorization.application.useCases.UpdateAuthorizationUseCase;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.issuing.Authorization;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks/authorizations")
public class AuthorizationWebhookController {
    private final SyncBalanceUseCase syncBalanceUseCase;
    private final CheckAuthorizationEligibilityUseCase checkAuthorizationEligibility;
    @Value("${stripe.webhook.secret.authorization}")
    private String webhookSecret;
    private final AsyncLogger logger;
    private final CreateAuthorizationUseCase createAuthorizationUseCase;
    private final UpdateAuthorizationUseCase updateAuthorizationUseCase;

    public AuthorizationWebhookController(
            AsyncLogger logger,
            CreateAuthorizationUseCase createAuthorizationUseCase,
            UpdateAuthorizationUseCase updateAuthorizationUseCase, SyncBalanceUseCase syncBalanceUseCase,
            CheckAuthorizationEligibilityUseCase checkAuthorizationEligibility) {
        this.checkAuthorizationEligibility = checkAuthorizationEligibility;
        this.logger = logger;
        this.createAuthorizationUseCase = createAuthorizationUseCase;
        this.updateAuthorizationUseCase = updateAuthorizationUseCase;
        this.syncBalanceUseCase = syncBalanceUseCase;
    }

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, this.webhookSecret);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("invalid signature");
        }

        switch (event.getType()) {
            case "issuing_authorization.created":
                handleAuthorizationCreated(event);
                break;
            case "issuing_authorization.request":
                return handleAuthorizationRequest(event);
            case "issuing_authorization.updated":
                handleAuthorizationUpdated(event);
                break;
        }
        return ResponseEntity.ok().build();
    }

    private void handleAuthorizationCreated(Event event) {
        StripeObject object = event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));

        Authorization authorization = (Authorization) object;
        
        CreateAuthorizationDto dto = new CreateAuthorizationDto(
                authorization.getCard().getFinancialAccount(),
                authorization.getId(),
                authorization.getCard().getId(),
                authorization.getApproved(),
                authorization.getAmount(),
                authorization.getCurrency(),
                mapAuthorizationStatus(authorization.getStatus()),
                authorization.getMerchantData().getName(),
                authorization.getMerchantData().getCategory(),
                authorization.getMerchantData().getCity(),
                authorization.getMerchantData().getCountry()
        );

        createAuthorizationUseCase.execute(dto);
        logger.info("STRIPE", "Issuing authorization created");
    }

    private ResponseEntity<Map<String, Object>> handleAuthorizationRequest(Event event) {
        boolean approved = false;
        try {
            StripeObject object = event.getDataObjectDeserializer()
                    .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
            Authorization authorization = (Authorization) object;
            approved = checkAuthorizationEligibility.execute(authorization.getCard().getId());
        } catch (Exception ex) {
            // A signed request that cannot be verified must receive an explicit decline.
            logger.info("STRIPE", "Authorization request declined: " + ex.getMessage());
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Stripe-Version", "2026-06-24.dahlia")
                .body(Map.of("approved", approved));
    }

    private void handleAuthorizationUpdated(Event event) {
        StripeObject object = event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));

        Authorization authorization = (Authorization) object;

        UpdateAuthorizationDto dto = new UpdateAuthorizationDto(
                authorization.getId(),
                authorization.getApproved(),
                authorization.getAmount(),
                mapAuthorizationStatus(authorization.getStatus()),
                authorization.getMerchantData().getName(),
                authorization.getMerchantData().getCategory(),
                authorization.getMerchantData().getCity(),
                authorization.getMerchantData().getCountry()
        );

        updateAuthorizationUseCase.execute(dto);
        logger.info("STRIPE", "Issuing authorization updated");
    }

    private AuthorizationStatus mapAuthorizationStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return AuthorizationStatus.PENDING;
        }

        return switch (stripeStatus) {
            case "pending" -> AuthorizationStatus.PENDING;
            case "closed" -> AuthorizationStatus.CLOSED;
            case "reversed" -> AuthorizationStatus.REVERSED;
            default -> AuthorizationStatus.PENDING;
        };
    }
}
