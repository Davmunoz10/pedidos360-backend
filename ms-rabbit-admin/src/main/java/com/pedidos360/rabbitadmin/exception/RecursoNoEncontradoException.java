package com.pedidos360.rabbitadmin.exception;

/** Se lanza cuando una cola o un exchange no existe (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
