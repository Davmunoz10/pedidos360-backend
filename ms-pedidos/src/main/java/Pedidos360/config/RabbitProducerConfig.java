package Pedidos360.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitProducerConfig {

    @Value("${app.rabbit.exchanges.pedidos}")
    private String pedidosExchangeName;

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchangeName, true, false);
    }
}