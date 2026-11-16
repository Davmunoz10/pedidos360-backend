package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BindingRequest(
        @NotBlank(message = "exchange es obligatorio")
        @Pattern(regexp = Reglas.NOMBRE_REGEX, message = "exchange " + Reglas.NOMBRE_MENSAJE)
        String exchange,

        @NotBlank(message = "queue es obligatorio")
        @Pattern(regexp = Reglas.NOMBRE_REGEX, message = "queue " + Reglas.NOMBRE_MENSAJE)
        String queue,

        @Pattern(regexp = Reglas.ROUTING_KEY_REGEX, message = "routingKey " + Reglas.ROUTING_KEY_MENSAJE)
        String routingKey) {
}