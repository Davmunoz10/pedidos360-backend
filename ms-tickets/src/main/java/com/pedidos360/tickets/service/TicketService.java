package com.pedidos360.tickets.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pedidos360.tickets.dto.PedidoEvento;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    public void generar(PedidoEvento evento) {
        if ("temporal".equals(evento.modo())) {
            log.warn("[TICKETS] Fallo temporal simulado para el pedido {}", evento.pedidoId());
            throw new IllegalStateException("Fallo temporal simulado en la generacion del ticket");
        }
        log.info("[TICKETS] Ticket TCK-{} generado para {} (total {})",
                evento.pedidoId(), evento.clienteEmail(), evento.total());
    }
}