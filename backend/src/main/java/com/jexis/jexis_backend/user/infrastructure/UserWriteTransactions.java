package com.jexis.jexis_backend.user.infrastructure;

import java.util.function.Supplier;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.jexis.jexis_backend.user.domain.exceptions.PhoneNumberExistsException;

/** Translates a phone conflict outside the transaction, including failures during commit. */
@Component
public class UserWriteTransactions {
    public static final String PHONE_CONSTRAINT = "uk_users_phone_number";
    private final TransactionTemplate transactions;

    public UserWriteTransactions(PlatformTransactionManager manager) {
        transactions = new TransactionTemplate(manager);
    }

    public <T> T execute(Supplier<T> write) {
        try {
            return transactions.execute(status -> write.get());
        } catch (RuntimeException ex) {
            throw translate(ex);
        }
    }

    public static RuntimeException translate(RuntimeException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && PHONE_CONSTRAINT.equals(violation.getConstraintName())) {
                return new PhoneNumberExistsException();
            }
        }
        return ex;
    }
}
