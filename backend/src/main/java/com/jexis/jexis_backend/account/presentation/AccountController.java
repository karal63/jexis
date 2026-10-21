package com.jexis.jexis_backend.account.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.GetUpdateLinkDto;
import com.jexis.jexis_backend.account.application.dto.PaginatedAccountsResponseDto;
import com.jexis.jexis_backend.account.application.dto.PaginatedAccountsAdminResponseDto;
import com.jexis.jexis_backend.account.application.useCases.*;
import com.stripe.model.AccountLink;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;
import com.jexis.jexis_backend.account.application.dto.AccountAdminResponseDto;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import org.springframework.data.domain.Page;

/**
 * AccountController
 * REST controller in the presentation layer responsible for exposing
 * account-related HTTP endpoints.
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated account use case
 * services (application layer).
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 * Author: Leo
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AccountController {
    private final CreateAccountUseCase createAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final GetAccountsUseCase getAccountsUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DtoHelper dtoHelper;
    private final GetUserAccountsUseCase getUserAccountsUseCase;
    private final GetUpdateLinkUseCase getUpdateLinkUseCase;

    /**
     * Returns a paginated list of accounts for admins.
     * Supports query params: page (1-based), pageSize, search
     * Endpoint: GET /admin/accounts
     */
    @GetMapping("/admin/accounts")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public PaginatedAccountsAdminResponseDto getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search) {
        Page<Account> accountsPage = getAccountsUseCase.execute(page, pageSize, search);

        List<AccountAdminResponseDto> items = accountsPage
                .stream()
                .map(dtoHelper::toAccountAdminDto)
                .toList();

        return new PaginatedAccountsAdminResponseDto(
                items,
                page,
                pageSize,
                accountsPage.getTotalElements(),
                accountsPage.getTotalPages());
    }

    /**
     * Returns a paginated list of accounts for the authenticated user.
     * Supports query params: page (1-based), pageSize, search
     * Endpoint: GET /accounts
     */
    @GetMapping("/accounts")
    public PaginatedAccountsResponseDto getUserAccounts(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search) {
        Page<Account> accountsPage = getUserAccountsUseCase.execute(authUser.id(), page, pageSize, search);

        List<AccountResponseDto> items = accountsPage
                .stream()
                .map(dtoHelper::toAccountDto)
                .toList();

        return new PaginatedAccountsResponseDto(
                items,
                page,
                pageSize,
                accountsPage.getTotalElements(),
                accountsPage.getTotalPages());
    }

    /**
     * Return a specific account.
     * This endpoint retrieves a specific account by delegating to the
     * getAccountsUseCase, which interacts with the repository to fetch the data.
     * Endpoint: GET /account/{id}
     *
     * @param accountId the ID of the user
     * @return the account with the specified ID
     */
    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("@accountAuthorization.canView(authentication.principal.id(), #accountId)")
    public AccountResponseDto get(@PathVariable UUID accountId) {
        Account account = getAccountUseCase.execute(accountId);
        return dtoHelper.toAccountDto(account);
    }

    /**
     * Handles account creation requests.
     * Accepts a {@link AuthUser} payload, delegates execution to the
     * createAccountUseCase, and returns the created {@link Account}.
     * Endpoint: POST /account/create
     *
     * @param user user principal
     * @return the newly created account
     */
    @PostMapping("/accounts/create")
    public AccountResponseDto create(@AuthenticationPrincipal AuthUser user) {
        return createAccountUseCase.execute(user);
    }

    /**
     * Handles account deletion requests.
     * Accepts a {@param id} in the path, delegates execution to the
     * deleteAccountUseCase
     * Endpoint: DELETE /account/delete/{id}
     *
     * @param accountId the ID of the account to delete
     * @return message confirming deletion of the account with the specified ID
     */
    @PostMapping("/accounts/{accountId}/delete")
    @PreAuthorize("@accountAuthorization.canDelete(authentication.principal.id(), #accountId)")
    public String delete(@PathVariable UUID accountId) {
        deleteAccountUseCase.execute(accountId);
        return "Account with ID " + accountId + " has been deleted.";
    }

    @GetMapping("/accounts/{accountId}/get-update-link")
    @PreAuthorize("@accountAuthorization.canEdit(authentication.principal.id(), #accountId)")
    public GetUpdateLinkDto getUpdateLink(@PathVariable UUID accountId) {
        AccountLink link = getUpdateLinkUseCase.execute(accountId);
        return new GetUpdateLinkDto(link.getUrl());
    }
}