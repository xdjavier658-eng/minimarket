package com.minimarket.dto.reporte;

import java.math.BigDecimal;

public class PagoPorMetodoDTO {
    private String metodoPago;
    private Long cantidadPagos;
    private BigDecimal totalMonto;
    private BigDecimal porcentaje;

    public PagoPorMetodoDTO() {}

    public PagoPorMetodoDTO(String metodoPago, Long cantidadPagos, BigDecimal totalMonto) {
        this.metodoPago = metodoPago;
        this.cantidadPagos = cantidadPagos;
        this.totalMonto = totalMonto;
    }

    // Constructor para Object[] de la consulta
    public PagoPorMetodoDTO(Object[] row) {
        this.metodoPago = (String) row[0];
        this.cantidadPagos = (Long) row[1];
        this.totalMonto = (BigDecimal) row[2];
    }

    // Getters y Setters
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Long getCantidadPagos() { return cantidadPagos; }
    public void setCantidadPagos(Long cantidadPagos) { this.cantidadPagos = cantidadPagos; }

    public BigDecimal getTotalMonto() { return totalMonto; }
    public void setTotalMonto(BigDecimal totalMonto) { this.totalMonto = totalMonto; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
}