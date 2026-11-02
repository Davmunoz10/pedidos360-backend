package com.pedidos360.rabbitadmin.dto;

import java.util.List;

/**
 * Formato unico de error de la API.
 */
public record ApiError(int status, String error, String mensaje, List<String> detalles, String timestamp) {
}
