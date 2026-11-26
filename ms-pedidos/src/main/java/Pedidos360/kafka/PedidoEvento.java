package Pedidos360.kafka;

import java.math.BigDecimal;

public record PedidoEvento(
        String eventoId,
        String tipo,
        String pedidoId,
        String clienteEmail,
        BigDecimal total,
        String fecha,
        String modo) {
}