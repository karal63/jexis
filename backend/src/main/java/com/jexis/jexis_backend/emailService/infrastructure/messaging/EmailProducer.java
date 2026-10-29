package com.jexis.jexis_backend.emailService.infrastructure.messaging;

import com.jexis.jexis_backend.emailService.infrastructure.outbox.EmailOutbox;
import com.jexis.jexis_backend.emailService.infrastructure.outbox.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailProducer {
    private final EmailOutboxRepository outbox;

    @Transactional
    public void sendEmailTask(EmailTask task) {
        outbox.save(new EmailOutbox(task));
    }
}
