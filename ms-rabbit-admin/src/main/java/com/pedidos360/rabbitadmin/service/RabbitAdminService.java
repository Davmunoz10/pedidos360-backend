package com.pedidos360.rabbitadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.pedidos360.rabbitadmin.dto.BindingRequest;
import com.pedidos360.rabbitadmin.dto.ExchangeRequest;
import com.pedidos360.rabbitadmin.dto.QueueInfoResponse;
import com.pedidos360.rabbitadmin.dto.QueueRequest;
import com.pedidos360.rabbitadmin.dto.Reglas;
import com.pedidos360.rabbitadmin.exception.RecursoDuplicadoException;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;

/**
 * Unico lugar del microservicio que habla con RabbitMQ.
 * Crea, consulta y elimina colas, exchanges y bindings. Los controladores solo llaman a estos metodos.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;

    public RabbitAdminService(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ===================== COLAS =====================

    public QueueRequest crearCola(QueueRequest req) {
        if (req.deadLetterRoutingKey() != null && req.deadLetterExchange() == null) {
            throw new IllegalArgumentException("deadLetterRoutingKey requiere indicar deadLetterExchange");
        }
        if (req.deadLetterExchange() != null && !existeExchange(req.deadLetterExchange())) {
            throw new RecursoNoEncontradoException(
                    "El exchange dead-letter '" + req.deadLetterExchange() + "' no existe");
        }
        if (consultarCola(req.name()) != null) {
            throw new RecursoDuplicadoException("La cola '" + req.name() + "' ya existe");
        }

        boolean durable = req.durable() == null || req.durable();
        QueueBuilder builder = durable ? QueueBuilder.durable(req.name()) : QueueBuilder.nonDurable(req.name());
        if (req.deadLetterExchange() != null) {
            builder = builder.deadLetterExchange(req.deadLetterExchange());
        }
        if (req.deadLetterRoutingKey() != null) {
            builder = builder.deadLetterRoutingKey(req.deadLetterRoutingKey());
        }
        if (req.messageTtlMs() != null) {
            builder = builder.ttl(req.messageTtlMs().intValue());
        }
        amqpAdmin.declareQueue(builder.build());

        log.info("[ADMIN] Cola creada: {} (durable={})", req.name(), durable);
        return new QueueRequest(req.name(), durable, req.deadLetterExchange(),
                req.deadLetterRoutingKey(), req.messageTtlMs());
    }

    public QueueInfoResponse obtenerCola(String name) {
        validarNombre(name, "name");
        QueueInfoResponse info = consultarCola(name);
        if (info == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        return info;
    }

    public void eliminarCola(String name) {
        validarNombre(name, "name");
        if (consultarCola(name) == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        amqpAdmin.deleteQueue(name);
        log.info("[ADMIN] Cola eliminada: {}", name);
    }

    // ===================== EXCHANGES =====================

    public ExchangeRequest crearExchange(ExchangeRequest req) {
        if (existeExchange(req.name())) {
            throw new RecursoDuplicadoException("El exchange '" + req.name() + "' ya existe");
        }
        boolean durable = req.durable() == null || req.durable();
        Exchange exchange = switch (req.type()) {
            case "direct" -> new DirectExchange(req.name(), durable, false);
            case "topic" -> new TopicExchange(req.name(), durable, false);
            case "fanout" -> new FanoutExchange(req.name(), durable, false);
            case "headers" -> new HeadersExchange(req.name(), durable, false);
            default -> throw new IllegalArgumentException("Tipo de exchange invalido: " + req.type());
        };
        amqpAdmin.declareExchange(exchange);

        log.info("[ADMIN] Exchange creado: {} (tipo={}, durable={})", req.name(), req.type(), durable);
        return new ExchangeRequest(req.name(), req.type(), durable);
    }

    public void eliminarExchange(String name) {
        validarNombre(name, "name");
        if (!existeExchange(name)) {
            throw new RecursoNoEncontradoException("El exchange '" + name + "' no existe");
        }
        amqpAdmin.deleteExchange(name);
        log.info("[ADMIN] Exchange eliminado: {}", name);
    }

    // ===================== BINDINGS =====================

    public BindingRequest crearBinding(BindingRequest req) {
        verificarExistencia(req.exchange(), req.queue());
        String routingKey = (req.routingKey() == null) ? "" : req.routingKey();
        amqpAdmin.declareBinding(
                new Binding(req.queue(), Binding.DestinationType.QUEUE, req.exchange(), routingKey, null));

        log.info("[ADMIN] Binding creado: exchange={} -> cola={} (routingKey='{}')",
                req.exchange(), req.queue(), routingKey);
        return new BindingRequest(req.exchange(), req.queue(), routingKey);
    }

    public void eliminarBinding(String exchange, String queue, String routingKey) {
        validarNombre(exchange, "exchange");
        validarNombre(queue, "queue");
        validarRoutingKey(routingKey);
        verificarExistencia(exchange, queue);
        String key = (routingKey == null) ? "" : routingKey;
        amqpAdmin.removeBinding(
                new Binding(queue, Binding.DestinationType.QUEUE, exchange, key, null));

        log.info("[ADMIN] Binding eliminado: exchange={} -> cola={} (routingKey='{}')", exchange, queue, key);
    }

    // ===================== APOYO =====================

    private void verificarExistencia(String exchange, String queue) {
        if (!existeExchange(exchange)) {
            throw new RecursoNoEncontradoException("El exchange '" + exchange + "' no existe");
        }
        if (consultarCola(queue) == null) {
            throw new RecursoNoEncontradoException("La cola '" + queue + "' no existe");
        }
    }

    /** Devuelve los datos de la cola o null si no existe (declaracion pasiva: no crea nada). */
    private QueueInfoResponse consultarCola(String name) {
        return rabbitTemplate.execute(channel -> {
            try {
                var respuesta = channel.queueDeclarePassive(name);
                return new QueueInfoResponse(name, respuesta.getMessageCount(), respuesta.getConsumerCount());
            } catch (Exception e) {
                return null;
            }
        });
    }

    private boolean existeExchange(String name) {
        Boolean existe = rabbitTemplate.execute(channel -> {
            try {
                channel.exchangeDeclarePassive(name);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        return Boolean.TRUE.equals(existe);
    }

    private void validarNombre(String valor, String campo) {
        if (valor == null || !valor.matches(Reglas.NOMBRE_REGEX)) {
            throw new IllegalArgumentException(campo + " " + Reglas.NOMBRE_MENSAJE);
        }
    }

    private void validarRoutingKey(String valor) {
        if (valor != null && !valor.matches(Reglas.ROUTING_KEY_REGEX)) {
            throw new IllegalArgumentException("routingKey " + Reglas.ROUTING_KEY_MENSAJE);
        }
    }
}