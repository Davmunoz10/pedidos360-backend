package com.pedidos360.ms_notificaciones.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.pedidos360.ms_notificaciones.kafka.EventoInvalidoException;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            @Value("${app.kafka.retry.interval-ms}") long intervaloMs,
            @Value("${app.kafka.retry.max-retries}") long maxReintentos) {
        DefaultErrorHandler manejador = new DefaultErrorHandler(
                (registro, excepcion) -> {
                    Throwable causa = (excepcion.getCause() != null) ? excepcion.getCause() : excepcion;
                    log.error("[KAFKA] Mensaje descartado. topic={} partition={} offset={} key={} motivo={}",
                            registro.topic(), registro.partition(), registro.offset(), registro.key(),
                            causa.getMessage());
                },
                new FixedBackOff(intervaloMs, maxReintentos));
        manejador.addNotRetryableExceptions(EventoInvalidoException.class);
        manejador.setCommitRecovered(true);
        return manejador;
    }
}