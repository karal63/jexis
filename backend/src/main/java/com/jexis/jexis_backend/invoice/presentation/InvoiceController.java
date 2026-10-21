package com.jexis.jexis_backend.invoice.presentation;

import java.util.UUID;

import com.jexis.jexis_backend.invoice.application.dto.InvoicePageAdminResponseDto;
import com.jexis.jexis_backend.invoice.application.dto.InvoicePageResponseDto;
import com.jexis.jexis_backend.invoice.application.useCases.PayInvoiceUseCase;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jexis.jexis_backend.invoice.application.useCases.GetAllInvoicesUseCase;
import com.jexis.jexis_backend.invoice.application.useCases.GetSubscriptionInvoicesUseCase;
import com.jexis.jexis_backend.invoice.domain.entities.Invoice;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InvoiceController {
    private final GetAllInvoicesUseCase getAllInvoicesUseCase;
    private final GetSubscriptionInvoicesUseCase getSubscriptionInvoicesUseCase;
    private final PayInvoiceUseCase payInvoiceUseCase;

    @GetMapping("/admin/invoices")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public InvoicePageAdminResponseDto list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return mapToPageAdminResponse(getAllInvoicesUseCase.execute(page, pageSize));
    }

    @GetMapping("/invoices/subscription/{subscriptionId}")
    @PreAuthorize("@invoiceAuthorization.canView(authentication.principal.id, #subscriptionId)")
    public InvoicePageResponseDto listBySubscription(
            @PathVariable UUID subscriptionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return mapToPageResponse(getSubscriptionInvoicesUseCase.execute(subscriptionId, page, pageSize));
    }

    @PostMapping("/invoices/pay/{invoiceId}")
    @PreAuthorize("@invoiceAuthorization.canPay(authentication.principal.id, #invoiceId)")
    public void payInvoice(@PathVariable UUID invoiceId) {
        payInvoiceUseCase.execute(invoiceId);
    }

    private InvoicePageResponseDto mapToPageResponse(Page<Invoice> invoicesPage) {
        return new InvoicePageResponseDto(
                invoicesPage.getContent(),
                invoicesPage.getNumber(),
                invoicesPage.getSize(),
                invoicesPage.getTotalElements(),
                invoicesPage.getTotalPages()
        );
    }

    private InvoicePageAdminResponseDto mapToPageAdminResponse(Page<Invoice> invoicesPage) {
        return new InvoicePageAdminResponseDto(
                invoicesPage.getContent(),
                invoicesPage.getNumber(),
                invoicesPage.getSize(),
                invoicesPage.getTotalElements(),
                invoicesPage.getTotalPages()
        );
    }
}
