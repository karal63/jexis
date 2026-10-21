package com.jexis.jexis_backend.emailService.infrastructure.messaging;

import com.jexis.jexis_backend.emailService.application.useCases.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeEmailTask(EmailTask task) {
        log.info("Received email task for: {}", task.getTo());
        try {
            emailService.sendMail(task.getTo(), task.getSubject(), task.getBody());
            log.info("Email sent successfully to: {}", task.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to: {}", task.getTo(), e);
        }
    }
}
