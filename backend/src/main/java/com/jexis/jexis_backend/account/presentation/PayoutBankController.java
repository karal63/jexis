package com.jexis.jexis_backend.account.presentation;

import com.jexis.jexis_backend.account.application.useCases.CreateBankAccountSessionUseCase;
import com.jexis.jexis_backend.account.application.useCases.LinkPayoutBankUseCase;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PayoutBankController {
    private final CreateBankAccountSessionUseCase createBankAccountSessionUseCase;
    private final LinkPayoutBankUseCase linkPayoutBankUseCase;

    @PostMapping("/accounts/{accountId}/bank-account-session")
    @PreAuthorize("@accountAuthorization.canEdit(authentication.principal.id(), #accountId)")
    public ResponseEntity<BankAccountSession> createBankAccountSession(
            @PathVariable UUID accountId, @RequestParam(defaultValue = "false") boolean manage) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BankAccountSession(createBankAccountSessionUseCase.execute(accountId, manage)));
    }

    @PostMapping("/accounts/{accountId}/payout-bank-setup")
    @PreAuthorize("@accountAuthorization.canEdit(authentication.principal.id(), #accountId)")
    public ResponseEntity<LinkPayoutBankUseCase.Setup> startPayoutBankSetup(@PathVariable UUID accountId)
            throws StripeException {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(linkPayoutBankUseCase.start(accountId));
    }

    @PostMapping("/accounts/{accountId}/payout-bank-setup/{setupIntentId}/complete")
    @PreAuthorize("@accountAuthorization.canEdit(authentication.principal.id(), #accountId)")
    public void completePayoutBankSetup(@PathVariable UUID accountId, @PathVariable String setupIntentId)
            throws StripeException {
        linkPayoutBankUseCase.finish(accountId, setupIntentId);
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<BankSetupError> handleBankSetupStripeError(StripeException error) {
        String message = error.getStripeError() == null
                ? "Stripe could not process bank setup. Please retry."
                : error.getStripeError().getMessage();
        return ResponseEntity.status(502).cacheControl(CacheControl.noStore())
                .body(new BankSetupError(message, error.getRequestId()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BankSetupError> handleBankSetupValidation(
            ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).cacheControl(CacheControl.noStore())
                .body(new BankSetupError(error.getReason(), null));
    }

    public record BankSetupError(String message, String requestId) {}

    public record BankAccountSession(String clientSecret) {}

}
