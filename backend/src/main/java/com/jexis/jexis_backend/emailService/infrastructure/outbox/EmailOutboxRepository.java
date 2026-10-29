package com.jexis.jexis_backend.emailService.infrastructure.outbox;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, UUID> {
    @Query(value = "SELECT * FROM email_outbox WHERE next_attempt_at <= CURRENT_TIMESTAMP "
            + "ORDER BY created_at LIMIT 20 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<EmailOutbox> lockPending();
}
