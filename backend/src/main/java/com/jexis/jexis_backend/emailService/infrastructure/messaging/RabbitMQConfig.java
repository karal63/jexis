package com.jexis.jexis_backend.emailService.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "email.exchange";
    // New name avoids incompatible redeclaration of the existing queue without DLX arguments.
    public static final String QUEUE = "email.delivery.queue";
    public static final String DEAD_EXCHANGE = "email.dead.exchange";
    public static final String DEAD_QUEUE = "email.dead.queue";
    public static final String ROUTING_KEY = "email.routing.key";

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(DEAD_QUEUE).build();
    }

    @Bean
    public Binding binding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(ROUTING_KEY);
    }

    @Bean
    public DirectExchange emailDeadExchange() {
        return new DirectExchange(DEAD_EXCHANGE);
    }

    @Bean
    public Queue emailDeadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding deadBinding(Queue emailDeadQueue, DirectExchange emailDeadExchange) {
        return BindingBuilder.bind(emailDeadQueue).to(emailDeadExchange).with(DEAD_QUEUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
