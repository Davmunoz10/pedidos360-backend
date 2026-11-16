package com.pedidos360.ms_notificaciones.config;

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

    @Value("${app.rabbit.queues.notificaciones}")
    private String notificacionesQueueName;

    @Value("${app.rabbit.dlqs.notificaciones}")
    private String notificacionesDlqName;

    @Value("${app.rabbit.routing-keys.notificaciones}")
    private String notificacionesKey;

    @Value("${app.rabbit.routing-keys.notificaciones-dlq}")
    private String notificacionesDlqKey;

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchangeName, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(notificacionesQueueName)
                .deadLetterExchange(dlxExchangeName)
                .deadLetterRoutingKey(notificacionesDlqKey)
                .build();
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(notificacionesDlqName).build();
    }

    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange()).with(notificacionesKey);
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(dlxExchange()).with(notificacionesDlqKey);
    }
}