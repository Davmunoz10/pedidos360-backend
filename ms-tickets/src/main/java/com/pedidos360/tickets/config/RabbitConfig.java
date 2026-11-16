package com.pedidos360.tickets.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbit.exchanges.pedidos}")
    private String pedidosExchangeName;

    @Value("${app.rabbit.exchanges.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbit.queues.tickets}")
    private String ticketsQueueName;

    @Value("${app.rabbit.queues.documentos}")
    private String documentosQueueName;

    @Value("${app.rabbit.dlqs.tickets}")
    private String ticketsDlqName;

    @Value("${app.rabbit.dlqs.documentos}")
    private String documentosDlqName;

    @Value("${app.rabbit.routing-keys.tickets}")
    private String ticketsKey;

    @Value("${app.rabbit.routing-keys.tickets-dlq}")
    private String ticketsDlqKey;

    @Value("${app.rabbit.routing-keys.documentos}")
    private String documentosKey;

    @Value("${app.rabbit.routing-keys.documentos-dlq}")
    private String documentosDlqKey;

    // ---------- Exchanges ----------
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchangeName, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // ---------- Tickets ----------
    @Bean
    public Queue ticketsQueue() {
        return QueueBuilder.durable(ticketsQueueName)
                .deadLetterExchange(dlxExchangeName)
                .deadLetterRoutingKey(ticketsDlqKey)
                .build();
    }

    @Bean
    public Queue ticketsDlq() {
        return QueueBuilder.durable(ticketsDlqName).build();
    }

    @Bean
    public Binding ticketsBinding() {
        return BindingBuilder.bind(ticketsQueue()).to(pedidosExchange()).with(ticketsKey);
    }

    @Bean
    public Binding ticketsDlqBinding() {
        return BindingBuilder.bind(ticketsDlq()).to(dlxExchange()).with(ticketsDlqKey);
    }

    // ---------- Documentos ----------
    @Bean
    public Queue documentosQueue() {
        return QueueBuilder.durable(documentosQueueName)
                .deadLetterExchange(dlxExchangeName)
                .deadLetterRoutingKey(documentosDlqKey)
                .build();
    }

    @Bean
    public Queue documentosDlq() {
        return QueueBuilder.durable(documentosDlqName).build();
    }

    @Bean
    public Binding documentosBinding() {
        return BindingBuilder.bind(documentosQueue()).to(pedidosExchange()).with(documentosKey);
    }

    @Bean
    public Binding documentosDlqBinding() {
        return BindingBuilder.bind(documentosDlq()).to(dlxExchange()).with(documentosDlqKey);
    }
}