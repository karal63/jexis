package com.jexis.jexis_backend.emailService.infrastructure.outbox;

import com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailTask;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_outbox")
@Getter
@NoArgsConstructor
public class EmailOutbox {
    @Id private UUID id;
    @Column(nullable = false, columnDefinition = "text") private String recipient;
    @Column(nullable = false, columnDefinition = "text") private String subject;
    @Column(nullable = false, columnDefinition = "text") private String body;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant nextAttemptAt;
    @Column(nullable = false) private int attempts;

    public EmailOutbox(EmailTask task) {
        id = UUID.randomUUID();
        recipient = task.getTo();
        subject = task.getSubject();
        body = task.getBody();
        createdAt = Instant.now();
        nextAttemptAt = createdAt;
    }

    public EmailTask task() { return new EmailTask(recipient, subject, body); }

    public void retryLater() {
        attempts++;
        nextAttemptAt = Instant.now().plusSeconds(Math.min(300, 5L << Math.min(attempts - 1, 6)));
    }
}
