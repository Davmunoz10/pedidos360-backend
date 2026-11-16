package com.pedidos360.tickets.consumer.tickets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import com.pedidos360.tickets.model.PedidoEvent;
import com.rabbitmq.client.Channel;

@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);
    private static final String HEADER_REINTENTOS = "x-retry-count";

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.queues.tickets}")
    private String colaTickets;

    @Value("${app.rabbit.retry.max-retries}")
    private int maxReintentos;

    public TicketConsumer(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.rabbit.queues.tickets}")
    public void recibir(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            PedidoEvent evento = leerEvento(cuerpo);
            procesar(evento);
            channel.basicAck(tag, false);
            log.info("[TICKETS] ACK pedido={}", evento.getPedidoId());
        } catch (IllegalArgumentException e) {
            log.error("[TICKETS] Mensaje invalido, NACK sin reintento -> DLQ. motivo={} cuerpo={}",
                    e.getMessage(), cuerpo);
            channel.basicNack(tag, false, false);
        } catch (Exception e) {
            manejarErrorTemporal(message, channel, tag, cuerpo, e);
        }
    }

        private PedidoEvent leerEvento(String cuerpo) {
        PedidoEvent evento;
        try {
            evento = objectMapper.readValue(cuerpo, PedidoEvent.class);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("JSON invalido: " + e.getMessage());
        }
        if (evento == null || evento.getPedidoId() == null || evento.getPedidoId().trim().isEmpty()) {
            throw new IllegalArgumentException("pedidoId es obligatorio");
        }
        return evento;
    }

    private void procesar(PedidoEvent evento) {
        if ("temporal".equals(evento.getModo())) {
            throw new IllegalStateException("Fallo temporal simulado");
        }
        log.info("[TICKETS] Ticket generado para pedido {} (estado {})",
                evento.getPedidoId(), evento.getEstado());
    }

    private void manejarErrorTemporal(Message message, Channel channel, long tag, String cuerpo,
            Exception error) throws IOException {
        MessageProperties props = message.getMessageProperties();
        int intentos = obtenerIntentos(props);
        if (intentos < maxReintentos) {
            props.setHeader(HEADER_REINTENTOS, intentos + 1);
            try {
                rabbitTemplate.send("", colaTickets, new Message(message.getBody(), props));
                channel.basicAck(tag, false);
                log.warn("[TICKETS] Error temporal ({}). Reintento {}/{}",
                        error.getMessage(), intentos + 1, maxReintentos);
            } catch (AmqpException ex) {
                log.error("[TICKETS] No se pudo republicar para reintento, se devuelve a la cola. motivo={}",
                        ex.getMessage());
                channel.basicNack(tag, false, true);
            }
        } else {
            log.error("[TICKETS] Reintentos agotados ({}). NACK -> DLQ. motivo={} cuerpo={}",
                    maxReintentos, error.getMessage(), cuerpo);
            channel.basicNack(tag, false, false);
        }
    }

    private int obtenerIntentos(MessageProperties props) {
        Object valor = props.getHeader(HEADER_REINTENTOS);
        return (valor instanceof Number) ? ((Number) valor).intValue() : 0;
    }
}