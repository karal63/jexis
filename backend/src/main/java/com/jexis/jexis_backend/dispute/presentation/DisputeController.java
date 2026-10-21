package com.jexis.jexis_backend.dispute.presentation;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.DisputePageResponseDto;
import com.jexis.jexis_backend.dispute.application.dto.DisputePageAdminResponseDto;
import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.jexis.jexis_backend.dispute.application.dto.DisputeResponseDto;
import com.jexis.jexis_backend.dispute.application.useCases.CreateDisputeUseCase;
import com.jexis.jexis_backend.dispute.application.useCases.GetCardDisputesUseCase;
import com.jexis.jexis_backend.dispute.application.useCases.GetDisputeUseCase;
import com.jexis.jexis_backend.dispute.application.useCases.GetDisputesUseCase;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RequestMapping("/")
@RestController
@RequiredArgsConstructor
public class DisputeController {
    private final CreateDisputeUseCase createDisputeUseCase;
    private final GetDisputesUseCase getDisputesUseCase;
    private final GetCardDisputesUseCase getCardDisputesUseCase;
    private final GetDisputeUseCase getDisputeUseCase;
    private final DtoHelper dtoHelper;

    @PostMapping("/dispute/create")
    @PreAuthorize("@disputeAuthorization.canCreateDispute(authentication.principal.id(), #body.transactionId())")
    public Dispute create(@Valid @RequestBody CreateDisputeDto body) {
        return createDisputeUseCase.execute(body);
    }

    @GetMapping("/admin/disputes")
    @PreAuthorize("@disputeAuthorization.canViewAll(authentication.principal.id())")
    public DisputePageAdminResponseDto getAllDisputes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DisputeReason reason,
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Dispute> disputesPage = getDisputesUseCase.execute(page, pageSize, search, reason, status,
                sortBy, sortDirection);
        return mapToPageAdminResponse(disputesPage, page, pageSize);
    }

    @GetMapping("/cards/{cardId}/disputes")
    @PreAuthorize("@disputeAuthorization.canViewCardDisputes(authentication.principal.id(), #cardId)")
    public DisputePageResponseDto getCardDisputes(
            @PathVariable UUID cardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DisputeReason reason,
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) String walletName,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Dispute> disputesPage = getCardDisputesUseCase.execute(cardId, page, pageSize, search, reason, status,
                walletName, sortBy, sortDirection);
        return mapToPageResponse(disputesPage, page, pageSize);
    }

    @GetMapping("/disputes/{id}")
    @PreAuthorize("@disputeAuthorization.canView(authentication.principal.id(), #id)")
    public DisputeResponseDto getDispute(@PathVariable UUID id) {
        Dispute dispute = getDisputeUseCase.execute(id);
        return dtoHelper.toDisputeDto(dispute);
    }

    private DisputePageResponseDto mapToPageResponse(Page<Dispute> disputesPage, int page, int pageSize) {
        List<DisputeResponseDto> items = disputesPage.getContent().stream()
                .map(dtoHelper::toDisputeDto)
                .toList();
        return new DisputePageResponseDto(
                items,
                page,
                pageSize,
                disputesPage.getTotalElements(),
                disputesPage.getTotalPages());
    }

    private DisputePageAdminResponseDto mapToPageAdminResponse(Page<Dispute> disputesPage, int page, int pageSize) {
        var items = disputesPage.getContent().stream()
                .map(dtoHelper::toDisputeAdminDto)
                .toList();
        return new DisputePageAdminResponseDto(
                items,
                page,
                pageSize,
                disputesPage.getTotalElements(),
                disputesPage.getTotalPages());
    }
}
