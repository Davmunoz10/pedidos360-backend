package com.pedidos360.ms_kafka_admin.exception;

public class KafkaNoDisponibleException extends RuntimeException {

    public KafkaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}