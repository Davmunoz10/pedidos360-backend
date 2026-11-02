package com.pedidos360.tickets.consumer.documentos;

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

/**
 * Consumidor del dominio DOCUMENTOS con ACK manual.
 *  - OK                  -> basicAck
 *  - Mensaje invalido    -> basicNack sin requeue -> DLQ (reintentar no sirve)
 *  - Error temporal      -> reintento: se republica con header x-retry-count y se hace ACK al original
 *  - Reintentos agotados -> basicNack sin requeue -> DLQ
 * Todo NACK queda registrado en el log.
 */
@Component
public class DocumentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentoConsumer.class);
    private static final String HEADER_REINTENTOS = "x-retry-count";

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.queues.documentos}")
    private String colaDocumentos;

    @Value("${app.rabbit.retry.max-retries}")
    private int maxReintentos;

    public DocumentoConsumer(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.rabbit.queues.documentos}")
    public void recibir(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String cuerpo = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            PedidoEvent evento = leerEvento(cuerpo);
            procesar(evento);
            channel.basicAck(tag, false);
            log.info("[DOCUMENTOS] ACK pedido={}", evento.getPedidoId());
        } catch (IllegalArgumentException e) {
            log.error("[DOCUMENTOS] Mensaje invalido, NACK sin reintento -> DLQ. motivo={} cuerpo={}",
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
        // Gancho de pruebas: modo "temporal" simula la caida momentanea de un servicio externo.
        if ("temporal".equals(evento.getModo())) {
            throw new IllegalStateException("Fallo temporal simulado");
        }
        log.info("[DOCUMENTOS] Documento generado para pedido {} (estado {})",
                evento.getPedidoId(), evento.getEstado());
    }

    private void manejarErrorTemporal(Message message, Channel channel, long tag, String cuerpo,
            Exception error) throws IOException {
        MessageProperties props = message.getMessageProperties();
        int intentos = obtenerIntentos(props);
        if (intentos < maxReintentos) {
            props.setHeader(HEADER_REINTENTOS, intentos + 1);
            try {
                rabbitTemplate.send("", colaDocumentos, new Message(message.getBody(), props));
                channel.basicAck(tag, false);
                log.warn("[DOCUMENTOS] Error temporal ({}). Reintento {}/{}",
                        error.getMessage(), intentos + 1, maxReintentos);
            } catch (AmqpException ex) {
                log.error("[DOCUMENTOS] No se pudo republicar para reintento, se devuelve a la cola. motivo={}",
                        ex.getMessage());
                channel.basicNack(tag, false, true);
            }
        } else {
            log.error("[DOCUMENTOS] Reintentos agotados ({}). NACK -> DLQ. motivo={} cuerpo={}",
                    maxReintentos, error.getMessage(), cuerpo);
            channel.basicNack(tag, false, false);
        }
    }

    private int obtenerIntentos(MessageProperties props) {
        Object valor = props.getHeader(HEADER_REINTENTOS);
        return (valor instanceof Number) ? ((Number) valor).intValue() : 0;
    }
}