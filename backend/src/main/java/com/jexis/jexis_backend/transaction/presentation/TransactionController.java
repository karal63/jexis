package com.jexis.jexis_backend.transaction.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.transaction.application.dto.TransactionPageResponseDto;
import com.jexis.jexis_backend.transaction.application.dto.TransactionPageAdminResponseDto;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.transaction.application.dto.TransactionResponseDto;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionsUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetWalletTransactionsUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetCardTransactionsUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;

/**
 * TransactionController
 * <p>
 * REST controller in the presentation layer responsible for exposing
 * transaction-related HTTP endpoints.
 * <p>
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated transaction use case
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
public class TransactionController {

    private final GetTransactionUseCase getTransactionUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final GetWalletTransactionsUseCase getWalletTransactionsUseCase;
    private final GetCardTransactionsUseCase getCardTransactionsUseCase;
    private final DtoHelper dtoHelper;

    public TransactionController(
            GetTransactionUseCase getTransactionUseCase,
            GetTransactionsUseCase getTransactionsUseCase,
            GetWalletTransactionsUseCase getWalletTransactionsUseCase,
            GetCardTransactionsUseCase getCardTransactionsUseCase,
            DtoHelper dtoHelper) {
        this.getTransactionUseCase = getTransactionUseCase;
        this.getTransactionsUseCase = getTransactionsUseCase;
        this.getWalletTransactionsUseCase = getWalletTransactionsUseCase;
        this.getCardTransactionsUseCase = getCardTransactionsUseCase;
        this.dtoHelper = dtoHelper;
    }

    /**
     * Retrieves all transactions.
     * Endpoint: GET /admin/transactions
     *
     * @return list of all transactions
     */
    @GetMapping("/admin/transactions")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public TransactionPageAdminResponseDto getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Transaction> pageResult =
                getTransactionsUseCase.execute(page, pageSize, search, type, status, direction, sortBy, sortDirection);
        return mapToPageAdminResponse(pageResult, page, pageSize);
    }

    /**
     * Retrieves a single transaction by its identifier.
     * Endpoint: GET /transactions/{id}
     *
     * @param id the unique identifier of the transaction to retrieve
     * @return the matching transaction entity
     */
    @GetMapping("/transactions/{id}")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @transactionAuthorization.canView(authentication.principal.id(), #id)")
    public TransactionResponseDto getTransaction(@PathVariable UUID id) {
        Transaction transaction = getTransactionUseCase.execute(id);
        return dtoHelper.toTransactionDto(transaction);
    }

    /**
     * Retrieves all transactions for a specific wallet.
     * Endpoint: GET /wallets/{walletId}/transactions
     *
     * @param walletId the unique identifier of the wallet
     * @return list of transactions for the wallet
     */
    @GetMapping("/wallets/{walletId}/transactions")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @transactionAuthorization.canViewWallet(authentication.principal.id(), #walletId)")
    public TransactionPageResponseDto getWalletTransactions(
            @PathVariable UUID walletId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Transaction> pageResult =
                getWalletTransactionsUseCase.execute(walletId, page, pageSize, search, type, status, direction, sortBy, sortDirection);
        return mapToPageResponse(pageResult, page, pageSize);
    }

    /**
     * Retrieves all transactions for a specific card.
     * Endpoint: GET /cards/{cardId}/transactions
     *
     * @param cardId the unique identifier of the card
     * @return list of transactions for the card
     */
    @GetMapping("/cards/{cardId}/transactions")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @cardAuthorization.canView(authentication.principal.id(), #cardId)")
    public TransactionPageResponseDto getCardTransactions(
            @PathVariable UUID cardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Transaction> pageResult =
                getCardTransactionsUseCase.execute(cardId, page, pageSize, search, type, status, direction, sortBy, sortDirection);
        return mapToPageResponse(pageResult, page, pageSize);
    }

    private TransactionPageResponseDto mapToPageResponse(Page<Transaction> transactionsPage, int page, int pageSize) {
        List<TransactionResponseDto> items = transactionsPage.getContent().stream()
                .map(dtoHelper::toTransactionDto)
                .toList();
        return new TransactionPageResponseDto(
                items,
                page,
                pageSize,
                transactionsPage.getTotalElements(),
                transactionsPage.getTotalPages());
    }

    private TransactionPageAdminResponseDto mapToPageAdminResponse(Page<Transaction> transactionsPage, int page, int pageSize) {
        var items = transactionsPage.getContent().stream()
                .map(dtoHelper::toTransactionAdminDto)
                .toList();
        return new TransactionPageAdminResponseDto(
                items,
                page,
                pageSize,
                transactionsPage.getTotalElements(),
                transactionsPage.getTotalPages());
    }
}

