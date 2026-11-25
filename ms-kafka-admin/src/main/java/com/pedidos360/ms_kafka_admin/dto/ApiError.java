package com.pedidos360.ms_kafka_admin.dto;

import java.util.List;

public record ApiError(int status, String error, String mensaje, List<String> detalles, String timestamp) {
}