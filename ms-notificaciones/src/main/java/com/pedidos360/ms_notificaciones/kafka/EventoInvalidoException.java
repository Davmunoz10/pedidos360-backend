package com.pedidos360.ms_notificaciones.kafka;

public class EventoInvalidoException extends RuntimeException {

    public EventoInvalidoException(String mensaje) {
        super(mensaje);
    }
}