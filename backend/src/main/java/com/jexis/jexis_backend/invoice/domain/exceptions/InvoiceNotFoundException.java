package com.jexis.jexis_backend.invoice.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;


public class InvoiceNotFoundException extends DomainException {
    public InvoiceNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "INVOICE_NOT_FOUND", "Invoice not found");
    }
}
