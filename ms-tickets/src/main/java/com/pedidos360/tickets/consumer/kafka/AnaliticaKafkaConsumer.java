package com.pedidos360.tickets.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pedidos360.tickets.dto.AnaliticaEvento;
import com.pedidos360.tickets.kafka.EventoJsonReader;
import com.pedidos360.tickets.service.AnaliticaService;

@Component
public class AnaliticaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnaliticaKafkaConsumer.class);

    private final EventoJsonReader lector;
    private final AnaliticaService servicio;

    public AnaliticaKafkaConsumer(EventoJsonReader lector, AnaliticaService servicio) {
        this.lector = lector;
        this.servicio = servicio;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.analitica}",
            groupId = "${app.kafka.groups.analitica}")
    public void recibir(ConsumerRecord<String, String> registro) {
        log.info("[KAFKA-ANALITICA] Recibido topic={} partition={} offset={} key={}",
                registro.topic(), registro.partition(), registro.offset(), registro.key());
        AnaliticaEvento evento = lector.leer(registro.value(), AnaliticaEvento.class);
        servicio.registrar(evento);
    }
}