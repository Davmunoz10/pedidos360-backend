package com.pedidos360.ms_notificaciones.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pedidos360.ms_notificaciones.dto.PedidoEvento;
import com.pedidos360.ms_notificaciones.kafka.EventoJsonReader;
import com.pedidos360.ms_notificaciones.service.NotificacionService;

@Component
public class NotificacionKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionKafkaConsumer.class);

    private final EventoJsonReader lector;
    private final NotificacionService servicio;

    public NotificacionKafkaConsumer(EventoJsonReader lector, NotificacionService servicio) {
        this.lector = lector;
        this.servicio = servicio;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.eventos}",
            groupId = "${app.kafka.groups.notificaciones}")
    public void recibir(ConsumerRecord<String, String> registro) {
        log.info("[KAFKA-NOTIFICACIONES] Recibido topic={} partition={} offset={} key={}",
                registro.topic(), registro.partition(), registro.offset(), registro.key());
        PedidoEvento evento = lector.leer(registro.value(), PedidoEvento.class);
        servicio.notificar(evento);
    }
}