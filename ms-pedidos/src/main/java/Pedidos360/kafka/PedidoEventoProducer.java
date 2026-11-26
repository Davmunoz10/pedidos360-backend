package Pedidos360.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class PedidoEventoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicEventos;
    private final String topicAnalitica;

    public PedidoEventoProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.eventos}") String topicEventos,
            @Value("${app.kafka.topics.analitica}") String topicAnalitica) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicEventos = topicEventos;
        this.topicAnalitica = topicAnalitica;
    }

    public void publicarPedidoCreado(String pedidoId, String clienteEmail, BigDecimal total) {
        publicar("PEDIDO_CREADO", pedidoId, clienteEmail, total, "ok");
    }

    public void publicar(String tipo, String pedidoId, String clienteEmail, BigDecimal total, String modo) {
        String fecha = Instant.now().toString();
        PedidoEvento evento = new PedidoEvento(
                UUID.randomUUID().toString(), tipo, pedidoId, clienteEmail, total, fecha, modo);
        AnaliticaEvento analitica = new AnaliticaEvento(pedidoId, clienteEmail, total, fecha);
        enviar(topicEventos, pedidoId, evento);
        enviar(topicAnalitica, pedidoId, analitica);
    }

    public void publicarMensajeInvalido(String clave) {
        enviarTexto(topicEventos, clave, "esto-no-es-json");
    }

    private void enviar(String topico, String clave, Object cuerpo) {
        try {
            enviarTexto(topico, clave, objectMapper.writeValueAsString(cuerpo));
        } catch (RuntimeException e) {
            log.error("[KAFKA-PRODUCTOR] No se pudo preparar el evento para {}: {}", topico, e.getMessage());
        }
    }

    private void enviarTexto(String topico, String clave, String texto) {
        try {
            kafkaTemplate.send(topico, clave, texto).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("[KAFKA-PRODUCTOR] Fallo al publicar en {}: {}", topico, error.getMessage());
                } else {
                    log.info("[KAFKA-PRODUCTOR] Publicado en {} particion={} offset={} key={}",
                            topico,
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(),
                            clave);
                }
            });
        } catch (RuntimeException e) {
            log.error("[KAFKA-PRODUCTOR] No se pudo publicar en {}: {}", topico, e.getMessage());
        }
    }
}