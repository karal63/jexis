package com.jexis.jexis_backend.externalAccount.presentation;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.externalAccount.application.dto.ExternalAccountPageResponseDto;
import com.jexis.jexis_backend.externalAccount.application.dto.ExternalAccountPageAdminResponseDto;
import com.jexis.jexis_backend.externalAccount.application.dto.ExternalAccountResponseDto;
import com.jexis.jexis_backend.externalAccount.application.useCases.GetAccountExternalAccountsUseCase;
import com.jexis.jexis_backend.externalAccount.application.useCases.GetExternalAccountUseCase;
import com.jexis.jexis_backend.externalAccount.application.useCases.GetExternalAccountsUseCase;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ExternalAccountController {
    private final GetExternalAccountsUseCase getExternalAccountsUseCase;
    private final GetAccountExternalAccountsUseCase getAccountExternalAccountsUseCase;
    private final GetExternalAccountUseCase getExternalAccountUseCase;
    private final DtoHelper dtoHelper;

    @GetMapping("/admin/external-accounts")
    @PreAuthorize("@externalAccountAuthorization.canViewAll(authentication.principal.id())")
    public ExternalAccountPageAdminResponseDto getAllExternalAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isDefault,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<ExternalAccount> externalAccountsPage = getExternalAccountsUseCase.execute(page, pageSize, search,
                isDefault, sortBy, sortDirection);
        return mapToPageAdminResponse(externalAccountsPage, page, pageSize);
    }

    @GetMapping("/accounts/{accountId}/external-accounts")
    @PreAuthorize("@externalAccountAuthorization.canViewAccountExternalAccounts(authentication.principal.id(), #accountId)")
    public ExternalAccountPageResponseDto getAccountExternalAccounts(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isDefault,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<ExternalAccount> externalAccountsPage = getAccountExternalAccountsUseCase.execute(accountId, page,
                pageSize, search, isDefault, sortBy, sortDirection);
        return mapToPageResponse(externalAccountsPage, page, pageSize);
    }

    @GetMapping("/external-accounts/{id}")
    @PreAuthorize("@externalAccountAuthorization.canView(authentication.principal.id(), #id)")
    public ExternalAccountResponseDto getExternalAccount(@PathVariable UUID id) {
        ExternalAccount externalAccount = getExternalAccountUseCase.execute(id);
        return dtoHelper.toExternalAccountDto(externalAccount);
    }

    private ExternalAccountPageResponseDto mapToPageResponse(Page<ExternalAccount> externalAccountsPage, int page,
            int pageSize) {
        return new ExternalAccountPageResponseDto(
                externalAccountsPage.getContent().stream()
                        .map(dtoHelper::toExternalAccountDto)
                        .toList(),
                page,
                pageSize,
                externalAccountsPage.getTotalElements(),
                externalAccountsPage.getTotalPages());
    }

    private ExternalAccountPageAdminResponseDto mapToPageAdminResponse(Page<ExternalAccount> externalAccountsPage, int page,
            int pageSize) {
        return new ExternalAccountPageAdminResponseDto(
                externalAccountsPage.getContent().stream()
                        .map(dtoHelper::toExternalAccountAdminDto)
                        .toList(),
                page,
                pageSize,
                externalAccountsPage.getTotalElements(),
                externalAccountsPage.getTotalPages());
    }
}
