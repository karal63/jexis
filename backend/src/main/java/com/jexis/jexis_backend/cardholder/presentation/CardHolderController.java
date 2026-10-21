package com.jexis.jexis_backend.cardholder.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jexis.jexis_backend.cardholder.application.dto.CardHolderPageResponseDto;
import com.jexis.jexis_backend.cardholder.application.dto.CardHolderPageAdminResponseDto;
import com.jexis.jexis_backend.cardholder.application.dto.CardHolderResponseDto;
import com.jexis.jexis_backend.cardholder.application.dto.CreateCardHolderDto;
import com.jexis.jexis_backend.cardholder.application.dto.EditCardHolderDto;
import com.jexis.jexis_backend.cardholder.application.useCases.CreateCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.DeleteCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.EditCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.GetAccountCardHoldersUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.GetAllCardHoldersUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.GetCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
public class CardHolderController {
    private final GetAllCardHoldersUseCase getAllCardHoldersUseCase;
    private final GetCardHolderUseCase getCardHolderUseCase;
    private final CreateCardHolderUseCase createCardHolderUseCase;
    private final EditCardHolderUseCase editCardHolderUseCase;
    private final DeleteCardHolderUseCase deleteCardHolderUseCase;
    private final DtoHelper dtoHelper;
    private final GetAccountCardHoldersUseCase getAccountCardHoldersUseCase;

    public CardHolderController(GetAllCardHoldersUseCase getAllCardHoldersUseCase,
            GetCardHolderUseCase getCardHolderUseCase, CreateCardHolderUseCase createCardHolderUseCase,
            EditCardHolderUseCase editCardHolderUseCase, DeleteCardHolderUseCase deleteCardHolderUseCase,
            DtoHelper dtoHelper, GetAccountCardHoldersUseCase getAccountCardHoldersUseCase) {
        this.getAllCardHoldersUseCase = getAllCardHoldersUseCase;
        this.getCardHolderUseCase = getCardHolderUseCase;
        this.createCardHolderUseCase = createCardHolderUseCase;
        this.editCardHolderUseCase = editCardHolderUseCase;
        this.deleteCardHolderUseCase = deleteCardHolderUseCase;
        this.dtoHelper = dtoHelper;
        this.getAccountCardHoldersUseCase = getAccountCardHoldersUseCase;
    }

    @GetMapping("/admin/card-holders")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public CardHolderPageAdminResponseDto list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardHolderStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<CardHolder> cardHolders = getAllCardHoldersUseCase.execute(page, pageSize, search, status, sortBy,
                sortDirection);
        return mapToPageAdminResponse(cardHolders, page, pageSize);
    }

    @PostMapping("/card-holder/create")
    @PreAuthorize("@cardHolderAuthorization.canCreate(authentication.principal.id(), #body.accountId)")
    public CardHolderResponseDto create(@Valid @RequestBody CreateCardHolderDto body, HttpServletRequest request) {
        CardHolder cardHolder = createCardHolderUseCase.execute(body, request);
        return dtoHelper.toCardHolderDto(cardHolder);
    }

    @GetMapping("/accounts/{id}/card-holders")
    @PreAuthorize("@cardHolderAuthorization.canView(authentication.principal.id(), #id)")
    public CardHolderPageResponseDto getCardHoldersByAccount(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CardHolderStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<CardHolder> cardHolder = getAccountCardHoldersUseCase.execute(id, page, pageSize, search, status, sortBy,
                sortDirection);
        return mapToPageResponse(cardHolder, page, pageSize);
    }

    @GetMapping("/accounts/{id}/card-holders/{cardHolderId}")
    @PreAuthorize("@cardHolderAuthorization.canView(authentication.principal.id(), #id, #cardHolderId)")
    @SuppressWarnings("unused")
    public CardHolderResponseDto find(@PathVariable UUID id, @PathVariable UUID cardHolderId) {
        CardHolder cardHolder = getCardHolderUseCase.execute(cardHolderId);
        return dtoHelper.toCardHolderDto(cardHolder);
    }

    @PatchMapping("/accounts/{id}/card-holders/{cardHolderId}/edit")
    @PreAuthorize("@cardHolderAuthorization.canEdit(authentication.principal.id(), #id, #cardHolderId)")
    @SuppressWarnings("unused")
    public CardHolderResponseDto edit(@PathVariable UUID id, @PathVariable UUID cardHolderId,
            @Valid @RequestBody EditCardHolderDto body) {
        CardHolder cardHolder = editCardHolderUseCase.execute(cardHolderId, body);
        return dtoHelper.toCardHolderDto(cardHolder);
    }

    @PostMapping("/accounts/{id}/card-holders/{cardHolderId}/delete")
    @PreAuthorize("@cardHolderAuthorization.canDelete(authentication.principal.id(), #id, #cardHolderId)")
    @SuppressWarnings("unused")
    public void delete(@PathVariable UUID id, @PathVariable UUID cardHolderId) {
        deleteCardHolderUseCase.execute(cardHolderId);
    }

    private CardHolderPageResponseDto mapToPageResponse(Page<CardHolder> cardHolderPage, int page, int pageSize) {
        List<CardHolderResponseDto> items = cardHolderPage.getContent().stream()
                .map(dtoHelper::toCardHolderDto)
                .toList();
        return new CardHolderPageResponseDto(
                items,
                page,
                pageSize,
                cardHolderPage.getTotalElements(),
                cardHolderPage.getTotalPages());
    }

    private CardHolderPageAdminResponseDto mapToPageAdminResponse(Page<CardHolder> cardHolderPage, int page, int pageSize) {
        var items = cardHolderPage.getContent().stream()
                .map(dtoHelper::toCardHolderAdminDto)
                .toList();
        return new CardHolderPageAdminResponseDto(
                items,
                page,
                pageSize,
                cardHolderPage.getTotalElements(),
                cardHolderPage.getTotalPages());
    }
}
