package com.minimarket.dto.reporte;

import java.math.BigDecimal;

public class TopProductoDTO {
    private Long productoId;
    private String nombre;
    private Long cantidadVendida;
    private BigDecimal totalVentas;

    public TopProductoDTO() {}

    public TopProductoDTO(Long productoId, String nombre, Long cantidadVendida, BigDecimal totalVentas) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidadVendida = cantidadVendida;
        this.totalVentas = totalVentas;
    }

    // Constructor para Object[] de la consulta
    public TopProductoDTO(Object[] row) {
        this.productoId = (Long) row[0];
        this.nombre = (String) row[1];
        this.cantidadVendida = (Long) row[2];
        this.totalVentas = (BigDecimal) row[3];
    }

    // Getters y Setters
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Long getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(Long cantidadVendida) { this.cantidadVendida = cantidadVendida; }

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }
}