package com.jexis.jexis_backend.member.presentation;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.common.web.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.jexis.jexis_backend.card.application.dto.CardPageResponseDto;
import com.jexis.jexis_backend.card.application.useCases.GetMemberCardsUseCase;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.transaction.application.dto.TransactionPageResponseDto;
import com.jexis.jexis_backend.transaction.application.useCases.GetMemberTransactionsUseCase;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.authorization.application.dto.AuthorizationPageResponseDto;
import com.jexis.jexis_backend.authorization.application.useCases.GetMemberAuthorizationsUseCase;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.dispute.application.dto.DisputePageResponseDto;
import com.jexis.jexis_backend.dispute.application.useCases.GetMemberDisputesUseCase;
import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/accounts/{accountId}/members/{memberId}")
@RequiredArgsConstructor
public class MemberResourceController {
    private final DtoHelper dtoHelper;
    private final GetMemberCardsUseCase getMemberCardsUseCase;
    private final GetMemberTransactionsUseCase getMemberTransactionsUseCase;
    private final GetMemberAuthorizationsUseCase getMemberAuthorizationsUseCase;
    private final GetMemberDisputesUseCase getMemberDisputesUseCase;

    @GetMapping("/cards")
    @PreAuthorize("@memberResourceAuthorization.canView(authentication.principal.id(), #accountId, #memberId)")
    public CardPageResponseDto getCards(
            @PathVariable UUID accountId,
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var result = getMemberCardsUseCase.execute(accountId, memberId, page, pageSize,
                search, status, brand, type, sortBy, sortDirection);
        return new CardPageResponseDto(result.getContent().stream().map(dtoHelper::toCardDto).toList(),
                page, pageSize, result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/transactions")
    @PreAuthorize("@memberResourceAuthorization.canView(authentication.principal.id(), #accountId, #memberId)")
    public TransactionPageResponseDto getTransactions(
            @PathVariable UUID accountId,
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var result = getMemberTransactionsUseCase.execute(accountId, memberId, page, pageSize,
                search, type, status, direction, sortBy, sortDirection);
        return new TransactionPageResponseDto(result.getContent().stream().map(dtoHelper::toTransactionDto).toList(),
                page, pageSize, result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/authorizations")
    @PreAuthorize("@memberResourceAuthorization.canView(authentication.principal.id(), #accountId, #memberId)")
    public AuthorizationPageResponseDto getAuthorizations(
            @PathVariable UUID accountId,
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) AuthorizationStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var result = getMemberAuthorizationsUseCase.execute(accountId, memberId, page, pageSize,
                search, approved, status, sortBy, sortDirection);
        return new AuthorizationPageResponseDto(result.getContent().stream().map(dtoHelper::toAuthorizationDto).toList(),
                page, pageSize, result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/disputes")
    @PreAuthorize("@memberResourceAuthorization.canView(authentication.principal.id(), #accountId, #memberId)")
    public DisputePageResponseDto getDisputes(
            @PathVariable UUID accountId,
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DisputeReason reason,
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var result = getMemberDisputesUseCase.execute(accountId, memberId, page, pageSize,
                search, reason, status, sortBy, sortDirection);
        return new DisputePageResponseDto(result.getContent().stream().map(dtoHelper::toDisputeDto).toList(),
                page, pageSize, result.getTotalElements(), result.getTotalPages());
    }

    @ExceptionHandler({HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> invalidQuery(Exception ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(400, "INVALID_QUERY", "Invalid pagination, filter or sort parameter"));
    }
}
