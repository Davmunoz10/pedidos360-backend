package Pedidos360.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion RabbitMQ del productor (ms-pedidos).
 * El nombre del exchange viene de application.properties (app.rabbit.exchanges.pedidos).
 */
@Configuration
public class RabbitProducerConfig {

    @Value("${app.rabbit.exchanges.pedidos}")
    private String pedidosExchangeName;

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchangeName, true, false);
    }
}