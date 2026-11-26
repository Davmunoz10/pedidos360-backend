package com.pedidos360.tickets.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnaliticaEvento(
        @NotBlank(message = "pedidoId es obligatorio")
        String pedidoId,

        @NotBlank(message = "clienteEmail es obligatorio")
        @Email(message = "clienteEmail no es un correo valido")
        String clienteEmail,

        @NotNull(message = "total es obligatorio")
        @DecimalMin(value = "0.0", message = "total no puede ser negativo")
        BigDecimal total,

        String fecha) {
}