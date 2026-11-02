package com.pedidos360.rabbitadmin.exception;

/** Se lanza cuando una cola o un exchange ya existe (HTTP 409). */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
