package com.jexis.jexis_backend.invoice.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jexis.jexis_backend.invoice.domain.entities.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findBySubscriptionId(UUID subscriptionId, Pageable pageable);

    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);
}
