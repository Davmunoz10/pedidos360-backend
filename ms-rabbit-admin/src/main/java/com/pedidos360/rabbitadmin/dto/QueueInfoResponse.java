package com.pedidos360.rabbitadmin.dto;

/**
 * Informacion basica de una cola existente.
 */
public record QueueInfoResponse(String name, int messageCount, int consumerCount) {
}
