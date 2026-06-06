package com.minimarket.dto.reporte;

import java.math.BigDecimal;

public class ReporteVentaMensualDTO {
    private Integer año;
    private Integer mes;
    private String nombreMes;
    private Long cantidadVentas;
    private BigDecimal totalVentas;

    public ReporteVentaMensualDTO() {}

    public ReporteVentaMensualDTO(Integer año, Integer mes, Long cantidadVentas, BigDecimal totalVentas) {
        this.año = año;
        this.mes = mes;
        this.nombreMes = obtenerNombreMes(mes);
        this.cantidadVentas = cantidadVentas;
        this.totalVentas = totalVentas;
    }

    // Constructor para Object[] de la consulta
    public ReporteVentaMensualDTO(Object[] row) {
        this.año = (Integer) row[0];
        this.mes = (Integer) row[1];
        this.nombreMes = obtenerNombreMes((Integer) row[1]);
        this.cantidadVentas = (Long) row[2];
        this.totalVentas = (BigDecimal) row[3];
    }

    private String obtenerNombreMes(Integer mes) {
        String[] meses = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return meses[mes - 1];
    }

    // Getters y Setters
    public Integer getAño() { return año; }
    public void setAño(Integer año) { this.año = año; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public String getNombreMes() { return nombreMes; }
    public void setNombreMes(String nombreMes) { this.nombreMes = nombreMes; }

    public Long getCantidadVentas() { return cantidadVentas; }
    public void setCantidadVentas(Long cantidadVentas) { this.cantidadVentas = cantidadVentas; }

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }
}