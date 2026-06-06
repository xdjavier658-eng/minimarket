package com.minimarket.dto.reporte;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO para estadísticas de pagos por método
 */
public class EstadisticasPorMetodoPagoDTO {
    
    private String metodoPago;
    private Long cantidadTransacciones;
    private Long cantidadExitosas;
    private BigDecimal montoTotal;
    private BigDecimal porcentajeDelTotal;
    private BigDecimal tasaExito;
    
    public EstadisticasPorMetodoPagoDTO() {}
    
    /**
     * Constructor para Object[] de consulta
     * @param row [metodoPago, cantidad, montoTotal, exitosos]
     */
    public EstadisticasPorMetodoPagoDTO(Object[] row) {
        this.metodoPago = ((Enum) row[0]).name();
        this.cantidadTransacciones = (Long) row[1];
        this.montoTotal = (BigDecimal) row[2];
        this.cantidadExitosas = (Long) row[3];
        
        calcularTasaExito();
    }
    
    private void calcularTasaExito() {
        if (cantidadTransacciones > 0) {
            this.tasaExito = BigDecimal.valueOf(cantidadExitosas)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(cantidadTransacciones), 2, RoundingMode.HALF_UP);
        } else {
            this.tasaExito = BigDecimal.ZERO;
        }
    }
    
    // Getters y Setters
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public Long getCantidadTransacciones() {
        return cantidadTransacciones;
    }
    
    public void setCantidadTransacciones(Long cantidadTransacciones) {
        this.cantidadTransacciones = cantidadTransacciones;
        calcularTasaExito();
    }
    
    public Long getCantidadExitosas() {
        return cantidadExitosas;
    }
    
    public void setCantidadExitosas(Long cantidadExitosas) {
        this.cantidadExitosas = cantidadExitosas;
        calcularTasaExito();
    }
    
    public BigDecimal getMontoTotal() {
        return montoTotal;
    }
    
    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
    
    public BigDecimal getPorcentajeDelTotal() {
        return porcentajeDelTotal;
    }
    
    public void setPorcentajeDelTotal(BigDecimal porcentajeDelTotal) {
        this.porcentajeDelTotal = porcentajeDelTotal;
    }
    
    public BigDecimal getTasaExito() {
        return tasaExito;
    }
    
    public void setTasaExito(BigDecimal tasaExito) {
        this.tasaExito = tasaExito;
    }
}