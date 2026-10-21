package com.jexis.jexis_backend.transaction.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import org.springframework.http.HttpStatus;

public class WrongTransactionStatusException extends DomainException {
    public WrongTransactionStatusException(TransactionStatus status) {
        super(HttpStatus.BAD_REQUEST.value(), "TRANSACTION_WRONG_STATUS", "Given wrong transaction status: " + status);
    }
}
