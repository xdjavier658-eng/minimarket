package com.minimarket.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResumenPeriodoDTO {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private BigDecimal totalVentas;
    private Long cantidadVentas;
    private BigDecimal ticketPromedio;
    private String productoMasVendido;
    private Long cantidadProductosVendidos;

    public ResumenPeriodoDTO() {}

    public ResumenPeriodoDTO(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Getters y Setters
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }

    public Long getCantidadVentas() { return cantidadVentas; }
    public void setCantidadVentas(Long cantidadVentas) { this.cantidadVentas = cantidadVentas; }

    public BigDecimal getTicketPromedio() { return ticketPromedio; }
    public void setTicketPromedio(BigDecimal ticketPromedio) { this.ticketPromedio = ticketPromedio; }

    public String getProductoMasVendido() { return productoMasVendido; }
    public void setProductoMasVendido(String productoMasVendido) { this.productoMasVendido = productoMasVendido; }

    public Long getCantidadProductosVendidos() { return cantidadProductosVendidos; }
    public void setCantidadProductosVendidos(Long cantidadProductosVendidos) { this.cantidadProductosVendidos = cantidadProductosVendidos; }
}