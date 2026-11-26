package com.pedidos360.tickets.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pedidos360.tickets.dto.PedidoEvento;
import com.pedidos360.tickets.kafka.EventoJsonReader;
import com.pedidos360.tickets.service.TicketService;

@Component
public class TicketKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketKafkaConsumer.class);

    private final EventoJsonReader lector;
    private final TicketService servicio;

    public TicketKafkaConsumer(EventoJsonReader lector, TicketService servicio) {
        this.lector = lector;
        this.servicio = servicio;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.eventos}",
            groupId = "${app.kafka.groups.tickets}")
    public void recibir(ConsumerRecord<String, String> registro) {
        log.info("[KAFKA-TICKETS] Recibido topic={} partition={} offset={} key={}",
                registro.topic(), registro.partition(), registro.offset(), registro.key());
        PedidoEvento evento = lector.leer(registro.value(), PedidoEvento.class);
        servicio.generar(evento);
    }
}