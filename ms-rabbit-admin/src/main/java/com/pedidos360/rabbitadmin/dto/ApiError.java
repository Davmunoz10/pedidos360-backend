package com.pedidos360.rabbitadmin.dto;

import java.util.List;

public record ApiError(int status, String error, String mensaje, List<String> detalles, String timestamp) {
}
