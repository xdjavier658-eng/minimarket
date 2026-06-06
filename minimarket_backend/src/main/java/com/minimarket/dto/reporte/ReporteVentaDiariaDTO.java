package com.minimarket.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReporteVentaDiariaDTO {
    private LocalDate fecha;
    private Long cantidadVentas;
    private BigDecimal totalVentas;

    public ReporteVentaDiariaDTO() {}

    public ReporteVentaDiariaDTO(LocalDate fecha, Long cantidadVentas, BigDecimal totalVentas) {
        this.fecha = fecha;
        this.cantidadVentas = cantidadVentas;
        this.totalVentas = totalVentas;
    }

    // Constructor para Object[] de la consulta
    public ReporteVentaDiariaDTO(Object[] row) {
        this.fecha = ((java.sql.Date) row[0]).toLocalDate();
        this.cantidadVentas = (Long) row[1];
        this.totalVentas = (BigDecimal) row[2];
    }

    // Getters y Setters
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Long getCantidadVentas() { return cantidadVentas; }
    public void setCantidadVentas(Long cantidadVentas) { this.cantidadVentas = cantidadVentas; }

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }
}