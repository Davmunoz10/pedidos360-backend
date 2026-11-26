package com.pedidos360.tickets.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pedidos360.tickets.dto.AnaliticaEvento;

@Service
public class AnaliticaService {

    private static final Logger log = LoggerFactory.getLogger(AnaliticaService.class);

    private long pedidosProcesados;
    private BigDecimal montoAcumulado = BigDecimal.ZERO;
    private final Map<String, BigDecimal> ventasPorCliente = new HashMap<>();

    public synchronized void registrar(AnaliticaEvento evento) {
        pedidosProcesados++;
        montoAcumulado = montoAcumulado.add(evento.total());
        ventasPorCliente.merge(evento.clienteEmail(), evento.total(), BigDecimal::add);
        log.info("[ANALITICA] Pedido {} registrado. Pedidos={} Monto acumulado={} Ventas de {}={}",
                evento.pedidoId(), pedidosProcesados, montoAcumulado,
                evento.clienteEmail(), ventasPorCliente.get(evento.clienteEmail()));
    }
}