package com.pedidos360.ms_kafka_admin.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pedidos360.ms_kafka_admin.dto.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDuplicado(RecursoDuplicadoException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(KafkaNoDisponibleException.class)
    public ResponseEntity<ApiError> manejarKafkaNoDisponible(KafkaNoDisponibleException ex) {
        log.error("[KAFKA-ADMIN] {}", ex.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null);
    }

    private ResponseEntity<ApiError> responder(HttpStatus status, String mensaje, List<String> detalles) {
        List<String> lista = (detalles == null) ? Collections.emptyList() : detalles;
        ApiError cuerpo = new ApiError(status.value(), status.getReasonPhrase(), mensaje, lista,
                Instant.now().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}