package Pedidos360.kafka;

import java.math.BigDecimal;

public record AnaliticaEvento(String pedidoId, String clienteEmail, BigDecimal total, String fecha) {
}