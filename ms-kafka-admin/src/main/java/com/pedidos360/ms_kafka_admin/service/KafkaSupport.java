package com.pedidos360.ms_kafka_admin.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pedidos360.ms_kafka_admin.exception.KafkaNoDisponibleException;
import com.pedidos360.ms_kafka_admin.exception.RecursoDuplicadoException;
import com.pedidos360.ms_kafka_admin.exception.RecursoNoEncontradoException;

@Component
public class KafkaSupport {

    private final long timeoutSegundos;

    public KafkaSupport(@Value("${app.kafka.admin.timeout-seconds}") long timeoutSegundos) {
        this.timeoutSegundos = timeoutSegundos;
    }

    public <T> T esperar(KafkaFuture<T> futuro) {
        try {
            return futuro.get(timeoutSegundos, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaNoDisponibleException("La operacion fue interrumpida");
        } catch (TimeoutException e) {
            throw new KafkaNoDisponibleException("Kafka no respondio a tiempo");
        } catch (ExecutionException e) {
            throw traducir(e.getCause());
        }
    }

    private RuntimeException traducir(Throwable causa) {
        if (causa instanceof UnknownTopicOrPartitionException) {
            return new RecursoNoEncontradoException("El topico no existe");
        }
        if (causa instanceof GroupIdNotFoundException) {
            return new RecursoNoEncontradoException("El consumer group no existe");
        }
        if (causa instanceof TopicExistsException) {
            return new RecursoDuplicadoException("El topico ya existe");
        }
        if (causa instanceof InvalidTopicException
                || causa instanceof InvalidConfigurationException
                || causa instanceof InvalidReplicationFactorException
                || causa instanceof InvalidPartitionsException
                || causa instanceof InvalidRequestException) {
            return new IllegalArgumentException(causa.getMessage());
        }
        return new KafkaNoDisponibleException("Kafka rechazo la operacion o no esta disponible: " + causa.getMessage());
    }
}