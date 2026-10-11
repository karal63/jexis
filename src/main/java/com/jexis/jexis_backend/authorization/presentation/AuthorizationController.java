package com.jexis.jexis_backend.authorization.presentation;

import com.jexis.jexis_backend.authorization.application.dto.AuthorizationPageResponseDto;
import com.jexis.jexis_backend.authorization.application.dto.AuthorizationPageAdminResponseDto;
import com.jexis.jexis_backend.authorization.application.dto.AuthorizationResponseDto;
import com.jexis.jexis_backend.authorization.application.useCases.GetAuthorizationUseCase;
import com.jexis.jexis_backend.authorization.application.useCases.GetAuthorizationsUseCase;
import com.jexis.jexis_backend.authorization.application.useCases.GetWalletAuthorizationsUseCase;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AuthorizationController
 * <p>
 * REST controller in the presentation layer responsible for exposing
 * authorization-related HTTP endpoints.
 * <p>
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated authorization use case
 * services (application layer).
 * <p>
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 * <p>
 * Author: Copilot
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthorizationController {
    private final GetAuthorizationUseCase getAuthorizationUseCase;
    private final GetAuthorizationsUseCase getAuthorizationsUseCase;
    private final GetWalletAuthorizationsUseCase getWalletAuthorizationsUseCase;
    private final DtoHelper dtoHelper;

    /**
     * Retrieves all authorizations with pagination and filtering.
     * Endpoint: GET /admin/authorizations
     *
     * @param page the page number (0-indexed)
     * @param pageSize the number of items per page
     * @param search optional search term for merchant name or other searchable fields
     * @param approved optional filter by approval status
     * @param status optional filter by authorization status
     * @return paginated authorization response
     */
    @GetMapping("/admin/authorizations")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public AuthorizationPageAdminResponseDto getAllAuthorizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) AuthorizationStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Authorization> authorizationPage = getAuthorizationsUseCase.execute(page, pageSize, search, approved, status, sortBy, sortDirection);
        return mapToPageAdminResponse(authorizationPage);
    }

    /**
     * Retrieves a single authorization by its identifier.
     * Endpoint: GET /authorizations/{id}
     *
     * @param id the unique identifier of the authorization to retrieve
     * @return the matching authorization entity
     */
    @GetMapping("/authorizations/{id}")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @authorizationAuthorization.canView(authentication.principal.id(), #id)")
    public AuthorizationResponseDto getAuthorization(@PathVariable UUID id) {
        Authorization authorization = getAuthorizationUseCase.execute(id);
        return dtoHelper.toAuthorizationDto(authorization);
    }

    /**
     * Retrieves all authorizations for a specific wallet with pagination and filtering.
     * Endpoint: GET /wallets/{walletId}/authorizations
     *
     * @param walletId the unique identifier of the wallet
     * @param page the page number (0-indexed)
     * @param pageSize the number of items per page
     * @param search optional search term for merchant name or other searchable fields
     * @param approved optional filter by approval status
     * @param status optional filter by authorization status
     * @return paginated authorization response for the wallet
     */
    @GetMapping("/wallets/{walletId}/authorizations")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @authorizationAuthorization.canViewWallet(authentication.principal.id(), #walletId)")
    public AuthorizationPageResponseDto getWalletAuthorizations(
            @PathVariable UUID walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) AuthorizationStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Authorization> authorizationPage = getWalletAuthorizationsUseCase.execute(walletId, page, pageSize, search, approved, status,  sortBy, sortDirection);
        return mapToPageResponse(authorizationPage);
    }

    private AuthorizationPageResponseDto mapToPageResponse(Page<Authorization> authorizationPage) {
        List<AuthorizationResponseDto> items = authorizationPage.getContent().stream()
                .map(dtoHelper::toAuthorizationDto)
                .toList();
        return new AuthorizationPageResponseDto(
                items,
                authorizationPage.getNumber(),
                authorizationPage.getSize(),
                authorizationPage.getTotalElements(),
                authorizationPage.getTotalPages()
        );
    }

    private AuthorizationPageAdminResponseDto mapToPageAdminResponse(Page<Authorization> authorizationPage) {
        var items = authorizationPage.getContent().stream()
                .map(dtoHelper::toAuthorizationAdminDto)
                .toList();
        return new AuthorizationPageAdminResponseDto(
                items,
                authorizationPage.getNumber(),
                authorizationPage.getSize(),
                authorizationPage.getTotalElements(),
                authorizationPage.getTotalPages()
        );
    }
}
