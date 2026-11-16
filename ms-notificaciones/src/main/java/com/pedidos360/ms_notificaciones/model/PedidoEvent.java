package com.pedidos360.ms_notificaciones.model;

public class PedidoEvent {

    private String pedidoId;
    private String estado;
    private String modo;

    public PedidoEvent() {
    }

    public PedidoEvent(String pedidoId, String estado, String modo) {
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.modo = modo;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    @Override
    public String toString() {
        return "PedidoEvent{pedidoId=" + pedidoId + ", estado=" + estado + ", modo=" + modo + "}";
    }
}