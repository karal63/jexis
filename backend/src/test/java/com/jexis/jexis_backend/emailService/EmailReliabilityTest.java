package com.jexis.jexis_backend.emailService;

import com.jexis.jexis_backend.emailService.application.useCases.EmailService;
import com.jexis.jexis_backend.emailService.infrastructure.messaging.*;
import com.jexis.jexis_backend.emailService.infrastructure.outbox.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:email_outbox;MODE=PostgreSQL;NON_KEYWORDS=KEY,VALUE,INTERVAL;DB_CLOSE_DELAY=-1")
class EmailReliabilityTest {
    @Autowired org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean EmailProducer producer;
    @Autowired com.jexis.jexis_backend.user.application.useCases.CreateUserUseCase createUser;
    @Autowired com.jexis.jexis_backend.user.infrastructure.UserRepository users;
    @Autowired com.jexis.jexis_backend.passwordResetToken.application.useCases.CreatePasswordResetTokenUseCase resetPassword;
    @Autowired com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository resetTokens;
    @Autowired EmailOutboxRepository outbox;
    @Autowired EmailOutboxPublisher publisher;
    @Autowired PlatformTransactionManager transactions;
    @MockitoBean RabbitTemplate rabbit;

    private final EmailTask task = new EmailTask("test@example.com", "Activation", "token-bearing body");

    @BeforeEach void clean() { outbox.deleteAll(); }

    public interface ContainerInvocation { void invoke(Object channel, Message message); }

    @Test void configuredListenerRetriesThreeTimesThenRejects() {
        var mail = mock(EmailService.class);
        doThrow(new MailSendException("SMTP unavailable")).when(mail).sendMail(any(), any(), any());
        var consumer = new EmailConsumer(mail);
        ContainerInvocation listener =
                (channel, message) -> consumer.consumeEmailTask(task);
        var proxy = new org.springframework.aop.framework.ProxyFactory(listener);
        var advice = (org.aopalliance.aop.Advice[]) org.springframework.test.util.ReflectionTestUtils
                .getField(rabbitListenerContainerFactory, "adviceChain");
        assertThat(advice).isNotEmpty();
        for (var interceptor : advice) proxy.addAdvice(interceptor);
        var retried = (ContainerInvocation) proxy.getProxy();
        assertThatThrownBy(() -> retried.invoke(null, new Message(new byte[0])))
                .hasCauseInstanceOf(org.springframework.amqp.AmqpRejectAndDontRequeueException.class);
        verify(mail, times(3)).sendMail(any(), any(), any());
        assertThat(new RabbitMQConfig().emailQueue().getArguments())
                .containsEntry("x-dead-letter-exchange", RabbitMQConfig.DEAD_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", RabbitMQConfig.DEAD_QUEUE);
    }

    @Test void outboxFailureRollsBackNewUser() {
        doThrow(new IllegalStateException("outbox unavailable")).when(producer).sendEmailTask(any());
        var request = new com.jexis.jexis_backend.user.application.dto.CreateDto(
                "Test", "User", "rollback@example.com", "+48111111111", "TestPassword123!");
        assertThatThrownBy(() -> createUser.execute(request,
                java.util.List.of(com.jexis.jexis_backend.user.domain.enums.UserRole.USER)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(users.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())).isEmpty();
        assertThat(outbox.count()).isZero();
    }

    @Test void outboxFailureRollsBackResetToken() {
        var user = users.save(new com.jexis.jexis_backend.user.domain.entities.User(
                "Test", "User", "reset-rollback@example.com", "+48222222222", "hash", "activation-hash",
                java.util.List.of(com.jexis.jexis_backend.user.domain.enums.UserRole.USER)));
        long before = resetTokens.count();
        doThrow(new IllegalStateException("outbox unavailable")).when(producer).sendEmailTask(any());
        assertThatThrownBy(() -> resetPassword.execute(user.getEmail())).isInstanceOf(IllegalStateException.class);
        assertThat(resetTokens.count()).isEqualTo(before);
        assertThat(outbox.count()).isZero();
        users.deleteById(user.getId());
    }

    @Test void smtpFailureEscapesConsumer() {
        EmailService mail = mock(EmailService.class);
        doThrow(new MailSendException("SMTP unavailable")).when(mail).sendMail(any(), any(), any());
        assertThatThrownBy(() -> new EmailConsumer(mail).consumeEmailTask(task))
                .isInstanceOf(MailSendException.class);
    }

    @Test void enqueueJoinsCallingTransaction() {
        new TransactionTemplate(transactions).executeWithoutResult(tx -> {
            producer.sendEmailTask(task);
            assertThat(outbox.count()).isEqualTo(1);
            tx.setRollbackOnly();
        });
        assertThat(outbox.count()).isZero();
        verifyNoInteractions(rabbit);
    }

    @Test void brokerFailureKeepsEmailAndBacksOff() {
        producer.sendEmailTask(task);
        doThrow(new IllegalStateException("offline")).when(rabbit).convertAndSend(
                anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));
        publisher.publishPending();
        var pending = outbox.findAll().get(0);
        assertThat(pending.getAttempts()).isEqualTo(1);
        assertThat(pending.getNextAttemptAt()).isAfter(pending.getCreatedAt());
        publisher.publishPending();
        verify(rabbit, times(1)).convertAndSend(anyString(), anyString(), any(Object.class),
                any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    private void confirm(boolean ack, boolean returned) {
        doAnswer(call -> {
            CorrelationData data = call.getArgument(4);
            if (returned) data.setReturned(new ReturnedMessage(new Message(new byte[0]), 312,
                    "NO_ROUTE", RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY));
            data.getFuture().complete(new CorrelationData.Confirm(ack, null));
            return null;
        }).when(rabbit).convertAndSend(anyString(), anyString(), any(Object.class),
                any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test void confirmedPublicationRemovesOutboxEntry() {
        producer.sendEmailTask(task);
        confirm(true, false);
        publisher.publishPending();
        assertThat(outbox.count()).isZero();
    }

    @Test void unroutablePublicationIsRetainedEvenWhenAcknowledged() {
        producer.sendEmailTask(task);
        confirm(true, true);
        publisher.publishPending();
        assertThat(outbox.findAll()).singleElement().satisfies(email -> assertThat(email.getAttempts()).isEqualTo(1));
    }

    @Test void nackRetainsEmail() {
        producer.sendEmailTask(task);
        confirm(false, false);
        publisher.publishPending();
        assertThat(outbox.count()).isEqualTo(1);
    }
}
