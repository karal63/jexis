package com.jexis.jexis_backend.emailService.infrastructure.outbox;

import com.jexis.jexis_backend.emailService.infrastructure.messaging.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOutboxPublisher {
    private final EmailOutboxRepository outbox;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void publishPending() {
        for (EmailOutbox email : outbox.lockPending()) {
            try {
                CorrelationData confirmation = new CorrelationData(email.getId().toString());
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY,
                        email.task(), message -> {
                            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            message.getMessageProperties().setMessageId(email.getId().toString());
                            return message;
                        }, confirmation);
                var result = confirmation.getFuture().get(10, TimeUnit.SECONDS);
                if (!result.ack() || confirmation.getReturned() != null) {
                    throw new IllegalStateException("Email publication was rejected or unroutable");
                }
                outbox.delete(email);
            } catch (Exception failure) {
                email.retryLater();
                // Avoid logging recipient, body, or token-bearing exception details.
                log.warn("Email outbox publication failed: id={}, attempts={}", email.getId(), email.getAttempts());
                if (failure instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
