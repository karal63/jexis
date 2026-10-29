package com.jexis.jexis_backend.emailService.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.email.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class EmailOutboxScheduler {
    private final EmailOutboxPublisher publisher;

    @Scheduled(fixedDelayString = "${application.email.outbox.poll-delay:5000}")
    public void publish() { publisher.publishPending(); }
}
