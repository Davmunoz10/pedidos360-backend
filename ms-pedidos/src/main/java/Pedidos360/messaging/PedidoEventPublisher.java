package Pedidos360.messaging;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Unico lugar de ms-pedidos que habla con RabbitMQ.
 * Si RabbitMQ falla, se registra el error pero NO se rompe el flujo del pedido.
 */
@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbit.exchanges.pedidos}")
    private String exchange;

    @Value("${app.rabbit.routing-keys.pedido-creado}")
    private String keyCreado;

    @Value("${app.rabbit.routing-keys.pedido-pagado}")
    private String keyPagado;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicarPedidoCreado(String pedidoId) {
        publicar(keyCreado, new PedidoEvent(pedidoId, "CREADO", "ok"));
    }

    public void publicarPedidoPagado(String pedidoId) {
        publicar(keyPagado, new PedidoEvent(pedidoId, "PAGADO", "ok"));
    }

    public void publicar(String routingKey, PedidoEvent evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);
            Message mensaje = MessageBuilder
                    .withBody(json.getBytes(StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();
            rabbitTemplate.send(exchange, routingKey, mensaje);
            log.info("[PEDIDOS] Evento publicado. key={} evento={}", routingKey, evento);
        } catch (RuntimeException e) {
            log.error("[PEDIDOS] No se pudo publicar el evento. key={} motivo={}", routingKey, e.getMessage());
        }
    }

    /** Solo para pruebas: publica un cuerpo que no es JSON valido. */
    public void publicarMensajeInvalido(String routingKey) {
        try {
            Message mensaje = MessageBuilder
                    .withBody("esto-no-es-json".getBytes(StandardCharsets.UTF_8))
                    .build();
            rabbitTemplate.send(exchange, routingKey, mensaje);
            log.info("[PEDIDOS] Mensaje INVALIDO publicado (prueba). key={}", routingKey);
        } catch (RuntimeException e) {
            log.error("[PEDIDOS] No se pudo publicar el mensaje de prueba. motivo={}", e.getMessage());
        }
    }
}