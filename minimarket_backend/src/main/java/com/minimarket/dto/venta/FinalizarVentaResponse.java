package com.minimarket.dto.venta;

import java.math.BigDecimal;

public class FinalizarVentaResponse {

    private Long ventaId;
    private String estado;
    private BigDecimal total;
    private String metodoPago;

    public Long getVentaId() {
        return ventaId;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
