package com.minimarket.dto.reporte;

import java.math.BigDecimal;

public class VentaPorVendedorDTO {
    private String vendedor;
    private Long cantidadVentas;
    private BigDecimal totalVentas;
    private BigDecimal promedioVenta;

    public VentaPorVendedorDTO() {}

    public VentaPorVendedorDTO(String vendedor, Long cantidadVentas, BigDecimal totalVentas) {
        this.vendedor = vendedor;
        this.cantidadVentas = cantidadVentas;
        this.totalVentas = totalVentas;
        this.promedioVenta = cantidadVentas > 0 
            ? totalVentas.divide(BigDecimal.valueOf(cantidadVentas), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    // Constructor para Object[] de la consulta
    public VentaPorVendedorDTO(Object[] row) {
        this.vendedor = (String) row[0];
        this.cantidadVentas = (Long) row[1];
        this.totalVentas = (BigDecimal) row[2];
        this.promedioVenta = this.cantidadVentas > 0 
            ? this.totalVentas.divide(BigDecimal.valueOf(this.cantidadVentas), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    // Getters y Setters
    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public Long getCantidadVentas() { return cantidadVentas; }
    public void setCantidadVentas(Long cantidadVentas) { this.cantidadVentas = cantidadVentas; }

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }

    public BigDecimal getPromedioVenta() { return promedioVenta; }
    public void setPromedioVenta(BigDecimal promedioVenta) { this.promedioVenta = promedioVenta; }
}