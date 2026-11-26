package com.pedidos360.tickets.kafka;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import tools.jackson.databind.ObjectMapper;

@Component
public class EventoJsonReader {

    private final ObjectMapper objectMapper;
    private final Validator validador;

    public EventoJsonReader(ObjectMapper objectMapper, Validator validador) {
        this.objectMapper = objectMapper;
        this.validador = validador;
    }

    public <T> T leer(String json, Class<T> tipo) {
        if (json == null || json.isBlank()) {
            throw new EventoInvalidoException("El mensaje esta vacio");
        }

        T evento;
        try {
            evento = objectMapper.readValue(json, tipo);
        } catch (RuntimeException e) {
            throw new EventoInvalidoException("JSON invalido: " + e.getMessage());
        }
        if (evento == null) {
            throw new EventoInvalidoException("El mensaje no contiene un evento");
        }

        Set<ConstraintViolation<T>> errores = validador.validate(evento);
        if (!errores.isEmpty()) {
            String detalle = errores.stream()
                    .map(error -> error.getPropertyPath() + ": " + error.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new EventoInvalidoException("Evento con datos invalidos: " + detalle);
        }
        return evento;
    }
}