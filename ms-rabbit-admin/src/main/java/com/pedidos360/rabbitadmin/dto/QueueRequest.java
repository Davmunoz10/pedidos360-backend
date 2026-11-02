package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para crear una cola.
 * durable: por defecto true. deadLetterExchange y deadLetterRoutingKey: opcionales (configuran la DLQ).
 * messageTtlMs: tiempo de vida de los mensajes en milisegundos (opcional).
 */
public record QueueRequest(
        @NotBlank(message = "name es obligatorio")
        @Pattern(regexp = Reglas.NOMBRE_REGEX, message = "name " + Reglas.NOMBRE_MENSAJE)
        String name,

        Boolean durable,

        @Pattern(regexp = Reglas.NOMBRE_REGEX, message = "deadLetterExchange " + Reglas.NOMBRE_MENSAJE)
        String deadLetterExchange,

        @Pattern(regexp = Reglas.ROUTING_KEY_REGEX, message = "deadLetterRoutingKey " + Reglas.ROUTING_KEY_MENSAJE)
        String deadLetterRoutingKey,

        @Min(value = 1, message = "messageTtlMs debe ser mayor a 0")
        @Max(value = 86400000, message = "messageTtlMs no puede superar 86400000 ms (24 horas)")
        Long messageTtlMs) {
}