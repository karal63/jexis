package com.jexis.jexis_backend.account.presentation;

import com.jexis.jexis_backend.account.application.dto.AccountResourceLimitDto;
import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountResourceLimitController {
    private final CheckAccountResourceLimitUseCase checkLimit;

    @GetMapping("/accounts/{accountId}/limits/{resource}")
    @PreAuthorize("@accountAuthorization.canView(authentication.principal.id(), #accountId)")
    public AccountResourceLimitDto check(@PathVariable UUID accountId, @PathVariable String resource,
            @RequestParam(defaultValue = "1") String additionalQuantity) {
        long quantity;
        try {
            if (!additionalQuantity.matches("[0-9]+")) throw new NumberFormatException();
            quantity = Long.parseLong(additionalQuantity);
        } catch (NumberFormatException ex) {
            throw new ResourceLimitException(400, "INVALID_QUANTITY", "Quantity must be a nonnegative integer");
        }
        return checkLimit.execute(accountId, AccountResource.fromPath(resource), quantity);
    }
}
