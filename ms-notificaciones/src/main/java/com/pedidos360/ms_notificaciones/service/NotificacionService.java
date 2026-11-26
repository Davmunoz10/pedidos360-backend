package com.pedidos360.ms_notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pedidos360.ms_notificaciones.dto.PedidoEvento;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void notificar(PedidoEvento evento) {
        if ("temporal".equals(evento.modo())) {
            log.warn("[NOTIFICACIONES] Fallo temporal simulado para el pedido {}", evento.pedidoId());
            throw new IllegalStateException("Fallo temporal simulado en el envio de la notificacion");
        }
        log.info("[NOTIFICACIONES] Notificacion enviada a {} por {} del pedido {} (total {})",
                evento.clienteEmail(), evento.tipo(), evento.pedidoId(), evento.total());
    }
}