package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Datos para crear un exchange. type: direct, topic, fanout o headers.
 */
public record ExchangeRequest(
        @NotBlank(message = "name es obligatorio")
        @Pattern(regexp = Reglas.NOMBRE_REGEX, message = "name " + Reglas.NOMBRE_MENSAJE)
        String name,

        @NotBlank(message = "type es obligatorio")
        @Pattern(regexp = "^(direct|topic|fanout|headers)$",
                message = "type debe ser direct, topic, fanout o headers")
        String type,

        Boolean durable) {
}