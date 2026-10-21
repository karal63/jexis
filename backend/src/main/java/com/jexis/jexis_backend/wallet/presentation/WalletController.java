package com.jexis.jexis_backend.wallet.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.stripe.application.useCases.StripeOutboundTransferUseCase;
import com.jexis.jexis_backend.wallet.application.dto.*;
import com.jexis.jexis_backend.wallet.application.useCases.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;

import jakarta.validation.Valid;

/**
 * WalletController
 * <p>
 * REST controller in the presentation layer responsible for exposing
 * wallet-related HTTP endpoints.
 * <p>
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated wallet use case
 * services (application layer).
 * <p>
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 * <p>
 * Author: Leo
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class WalletController {

    private final GetAllWalletsUseCase getAllWalletsUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final EditWalletUseCase editWalletUseCase;
    private final CloseWalletUseCase deleteWalletUseCase;
    private final DtoHelper dtoHelper;
    private final GetAccountWalletsUseCase getAccountWalletsUseCase;
    private final AddMoneyUseCase addMoneyUseCase;
    private final CreateOutboundTransferUseCase createOutboundTransferUseCase;
    private final CancelOutboundTransferUseCase cancelOutboundTransferUseCase;

    /**
     * Retrieves all wallets available in the system.
     * Endpoint: GET /wallet/list
     *
     * @return a list of all wallet entities
     */
    @GetMapping("/admin/wallets")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public WalletPageAdminResponseDto list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Wallet> pageResult = getAllWalletsUseCase.execute(page, pageSize, search, sortBy, sortDirection);
        return mapToPageAdminResponse(pageResult, page, pageSize);
    }

    /**
     * Creates a new wallet for the specified account.
     * Endpoint: POST /wallet/create
     *
     * @param body the request payload containing the account identifier
     * @return the newly created wallet entity
     */
    @PostMapping("/wallets/create-treasury-account")
    @PreAuthorize("@walletAuthorization.canCreate(authentication.principal.id(), #body.accountId)")
    public WalletResponseDto create(@Valid @RequestBody CreateWalletDto body) {
        Account account = getAccountUseCase.execute(body.getAccountId());
        Wallet wallet = createWalletUseCase.execute(account, body.getName());
        return dtoHelper.toWalletDto(wallet);
    }

    @GetMapping("/accounts/{id}/wallets")
    @PreAuthorize("@walletAuthorization.canView(authentication.principal.id(), #id)")
    public WalletPageResponseDto getWalletsByAccount(@PathVariable UUID id,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize,
                                                     @RequestParam(required = false) String search,
                                                     @RequestParam(required = false) String sortBy,
                                                     @RequestParam(required = false) String sortDirection) {
        Page<Wallet> pageResult = getAccountWalletsUseCase.execute(id, page, pageSize, search, sortBy, sortDirection);
        return mapToPageResponse(pageResult, page, pageSize);
    }

    @GetMapping("/accounts/{id}/wallets/{walletId}")
    @PreAuthorize("@walletAuthorization.canView(authentication.principal.id(), #id, #walletId)")
    public WalletResponseDto find(@PathVariable UUID id, @PathVariable UUID walletId) {
        Wallet wallet = getWalletUseCase.execute(walletId);
        return dtoHelper.toWalletDto(wallet);
    }

    /**
     * Updates an existing wallet with the provided changes.
     * Endpoint: PATCH /wallet/edit/{id}
     *
     * @param id   the unique identifier of the wallet to update
     * @param body the wallet update payload
     * @return the updated wallet entity
     */
    @PatchMapping("/accounts/{id}/wallets/{walletId}/edit")
    @PreAuthorize("@walletAuthorization.canEdit(authentication.principal.id(), #id, #walletId)")
    public WalletResponseDto edit(@PathVariable UUID id, @PathVariable UUID walletId, @Valid @RequestBody EditWalletDto body) {
        Wallet wallet = editWalletUseCase.execute(walletId, body);
        return dtoHelper.toWalletDto(wallet);
    }

    /**
     * Deletes a wallet owned by the authenticated user.
     * Endpoint: POST /wallet/delete/{id}
     *
     * @param id the unique identifier of the wallet to delete
     */
    @PostMapping("/accounts/{id}/wallets/{walletId}/close")
    @PreAuthorize("@walletAuthorization.canClose(authentication.principal.id(), #id, #walletId)")
    public ResponseEntity<?> close(@PathVariable UUID id, @PathVariable UUID walletId) {
        deleteWalletUseCase.execute(id, walletId);
        return ResponseEntity.ok("Wallet has been closed");
    }

    @PostMapping("/wallets/{id}/create-outbound-transfer")
    @PreAuthorize("@walletAuthorization.canCreateOutboundTransfers(authentication.principal.id(), #id, #body.externalAccountId())")
    public ResponseEntity<?> createOutboundTransfer(@PathVariable UUID id, @Valid @RequestBody CreateOutboundTransferDto body) {
        createOutboundTransferUseCase.execute(id, body);
        return ResponseEntity.ok("Outbound transfer has been created");
    }

    @PostMapping("/wallets/cancel-outbound-transfer/{transactionId}")
    @PreAuthorize("@walletAuthorization.canCancelOutboundTransfers(authentication.principal.id(), #transactionId)")
    public ResponseEntity<?> cancelOutboundTransfer(@PathVariable UUID transactionId) {
        cancelOutboundTransferUseCase.execute(transactionId);
        return ResponseEntity.ok("Outbound transfer has been canceled");
    }

    /**
     * AddMoney
     * Allows to add money to selected financial account
     *
     * @param id       represents accountId
     * @param walletId represents wallet id
     * @return positive message
     */
    @PostMapping("/test/accounts/{id}/wallets/{walletId}/add-money")
    @PreAuthorize("@walletAuthorization.canAddMoney(authentication.principal.id(), #id, #walletId)")
    public ResponseEntity<?> addMoney(@PathVariable UUID id, @PathVariable UUID walletId, @Valid @RequestBody AddReceivedCreditsDto body) {
        addMoneyUseCase.execute(id, walletId, body);
        return ResponseEntity.ok("Money has been added");
    }

    private WalletPageAdminResponseDto mapToPageAdminResponse(Page<Wallet> walletsPage, int page, int pageSize) {
        List<WalletAdminResponseDto> items = walletsPage.getContent().stream()
                .map(dtoHelper::toWalletAdminDto)
                .toList();
        return new WalletPageAdminResponseDto(
                items,
                page,
                pageSize,
                walletsPage.getTotalElements(),
                walletsPage.getTotalPages());
    }

    private WalletPageResponseDto mapToPageResponse(Page<Wallet> walletsPage, int page, int pageSize) {
        List<WalletResponseDto> items = walletsPage.getContent().stream()
                .map(dtoHelper::toWalletDto)
                .toList();
        return new WalletPageResponseDto(
                items,
                page,
                pageSize,
                walletsPage.getTotalElements(),
                walletsPage.getTotalPages());
    }

}
