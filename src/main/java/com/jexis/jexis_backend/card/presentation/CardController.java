package com.jexis.jexis_backend.card.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.card.application.dto.TestCardPaymentDto;
import com.jexis.jexis_backend.card.application.useCases.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jexis.jexis_backend.card.application.dto.CardPageResponseDto;
import com.jexis.jexis_backend.card.application.dto.CardPageAdminResponseDto;
import com.jexis.jexis_backend.card.application.dto.CardResponseDto;
import com.jexis.jexis_backend.card.application.dto.CreateCardDto;
import com.jexis.jexis_backend.card.application.dto.EditCardDto;
import com.jexis.jexis_backend.card.application.dto.ReplaceCardDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;

import jakarta.validation.Valid;

/**
 * CardController
 * REST controller in the presentation layer responsible for exposing
 * card-related HTTP endpoints.
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated card use case
 * services (application layer).
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 * Author: Leo
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class CardController {
    private final GetAllCardsUseCase getAllCardsUseCase;
    private final GetCardUseCase getCardUseCase;
    private final CreateCardUseCase createCardUseCase;
    private final EditCardUseCase editCardUseCase;
    private final DeleteCardUseCase deleteCardUseCase;
    private final ReplaceCardUseCase replaceCardUseCase;
    private final DtoHelper dtoHelper;
    private final GetWalletCardsUseCase getWalletCardsUseCase;
    private final TestCardPaymentUseCase testCardPaymentUseCase;

    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    @GetMapping("/admin/cards")
    public CardPageAdminResponseDto list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Card> cardsPage = getAllCardsUseCase.execute(page, pageSize, search, status, brand, type, sortBy,
                sortDirection);
        return mapToPageAdminResponse(cardsPage, page, pageSize);
    }

    /**
     * Creates a new card for the specified cardholder.
     * Endpoint: POST /card/create
     *
     * @param body the request payload containing card details and cardholder
     *             information
     * @return the newly created card entity
     */
    @PostMapping("/cards/create")
    @PreAuthorize("@cardAuthorization.canCreate(authentication.principal.id(), #body.accountId)")
    public CardResponseDto create(@Valid @RequestBody CreateCardDto body) {
        Card card = createCardUseCase.execute(body);
        return dtoHelper.toCardDto(card);
    }

    @GetMapping("/wallets/{id}/cards")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @cardAuthorization.canViewAll(authentication.principal.id(), #id)")
    public CardPageResponseDto getCardsByAccount(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Card> cardsPage = getWalletCardsUseCase.execute(id, page, pageSize, search, status, brand, type, sortBy,
                sortDirection);
        return mapToPageResponse(cardsPage, page, pageSize);
    }

    /**
     * Retrieves a single card by its identifier.
     * Endpoint: GET /card/list/{id}
     *
     * @return the matching card entity
     */

    // Now work on below endpoints, change authorization
    @GetMapping("/cards/{cardId}")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles()) or @cardAuthorization.canView(authentication.principal.id(), #cardId)")
    public CardResponseDto find(@PathVariable UUID cardId) {
        Card card = getCardUseCase.execute(cardId);
        return dtoHelper.toCardDto(card);
    }

    /**
     * Updates an existing card with the provided changes.
     *
     * Endpoint: PATCH /card/edit/{id}
     *
     * @param body the card update payload
     * @return the updated card entity
     */
    @PatchMapping("/cards/{cardId}/edit")
    @PreAuthorize("@cardAuthorization.canEdit(authentication.principal.id(), #cardId)")
    public CardResponseDto edit(@PathVariable UUID cardId, @Valid @RequestBody EditCardDto body) {
        Card card = editCardUseCase.execute(cardId, body);
        return dtoHelper.toCardDto(card);
    }

    @PostMapping("/cards/{cardId}/replace")
    @PreAuthorize("@cardAuthorization.canEdit(authentication.principal.id(), #cardId)")
    public CardResponseDto replace(@PathVariable UUID cardId,
            @Valid @RequestBody ReplaceCardDto body) {
        Card card = replaceCardUseCase.execute(cardId, body);
        return dtoHelper.toCardDto(card);
    }

    /**
     * Deletes a card owned by the authenticated user.
     * Endpoint: POST /card/delete/{id}
     *
     * @param cardId card id
     */
    @PostMapping("/cards/{cardId}/delete")
    @PreAuthorize("@cardAuthorization.canDelete(authentication.principal.id(), #cardId)")
    public void delete(@PathVariable UUID cardId) {
        deleteCardUseCase.execute(cardId);
    }

    /**
     * createTestPayment
     * <p>
     * API endpoint for simulating card transaction
     * </p>
     *
     * @param body contains payment data
     */
    @PostMapping("/cards/test-card-payment")
    public ResponseEntity<String> createTestPayment(@Valid @RequestBody TestCardPaymentDto body) {
        testCardPaymentUseCase.execute(body);
        return ResponseEntity.ok("Test payment created");
    }

    private CardPageResponseDto mapToPageResponse(Page<Card> cardsPage, int page, int pageSize) {
        List<CardResponseDto> items = cardsPage.getContent().stream()
                .map(dtoHelper::toCardDto)
                .toList();
        return new CardPageResponseDto(
                items,
                page,
                pageSize,
                cardsPage.getTotalElements(),
                cardsPage.getTotalPages());
    }

    private CardPageAdminResponseDto mapToPageAdminResponse(Page<Card> cardsPage, int page, int pageSize) {
        var items = cardsPage.getContent().stream()
                .map(dtoHelper::toCardAdminDto)
                .toList();
        return new CardPageAdminResponseDto(
                items,
                page,
                pageSize,
                cardsPage.getTotalElements(),
                cardsPage.getTotalPages());
    }

}
