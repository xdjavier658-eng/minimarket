package com.minimarket.dto.reporte;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO para estadísticas de transacciones de pago
 */
public class EstadisticasTransaccionesDTO {
    
    private Long totalTransacciones;
    private Long transaccionesExitosas;
    private Long transaccionesFallidas;
    private Long transaccionesPendientes;
    private Long transaccionesExpiradas;
    
    private BigDecimal montoTotalExitoso;
    private BigDecimal montoTotalFallido;
    
    private BigDecimal tasaConversion;
    private BigDecimal ticketPromedio;
    
    public EstadisticasTransaccionesDTO() {
        this.totalTransacciones = 0L;
        this.transaccionesExitosas = 0L;
        this.transaccionesFallidas = 0L;
        this.transaccionesPendientes = 0L;
        this.transaccionesExpiradas = 0L;
        this.montoTotalExitoso = BigDecimal.ZERO;
        this.montoTotalFallido = BigDecimal.ZERO;
        this.tasaConversion = BigDecimal.ZERO;
        this.ticketPromedio = BigDecimal.ZERO;
    }
    
    /**
     * Constructor para resultado de consulta
     * @param row Object[] de la consulta
     */
    public EstadisticasTransaccionesDTO(Object[] row) {
        this.totalTransacciones = (Long) row[0];
        this.transaccionesExitosas = (Long) row[1];
        this.transaccionesFallidas = (Long) row[2];
        this.transaccionesPendientes = (Long) row[3];
        this.montoTotalExitoso = (BigDecimal) row[4];
        
        calcularTasaConversion();
    }
    
    private void calcularTasaConversion() {
        if (totalTransacciones > 0) {
            this.tasaConversion = BigDecimal.valueOf(transaccionesExitosas)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP);
        }
        
        if (transaccionesExitosas > 0) {
            this.ticketPromedio = montoTotalExitoso
                    .divide(BigDecimal.valueOf(transaccionesExitosas), 2, RoundingMode.HALF_UP);
        }
    }
    
    // Getters y Setters
    public Long getTotalTransacciones() {
        return totalTransacciones;
    }
    
    public void setTotalTransacciones(Long totalTransacciones) {
        this.totalTransacciones = totalTransacciones;
    }
    
    public Long getTransaccionesExitosas() {
        return transaccionesExitosas;
    }
    
    public void setTransaccionesExitosas(Long transaccionesExitosas) {
        this.transaccionesExitosas = transaccionesExitosas;
        calcularTasaConversion();
    }
    
    public Long getTransaccionesFallidas() {
        return transaccionesFallidas;
    }
    
    public void setTransaccionesFallidas(Long transaccionesFallidas) {
        this.transaccionesFallidas = transaccionesFallidas;
    }
    
    public Long getTransaccionesPendientes() {
        return transaccionesPendientes;
    }
    
    public void setTransaccionesPendientes(Long transaccionesPendientes) {
        this.transaccionesPendientes = transaccionesPendientes;
    }
    
    public Long getTransaccionesExpiradas() {
        return transaccionesExpiradas;
    }
    
    public void setTransaccionesExpiradas(Long transaccionesExpiradas) {
        this.transaccionesExpiradas = transaccionesExpiradas;
    }
    
    public BigDecimal getMontoTotalExitoso() {
        return montoTotalExitoso;
    }
    
    public void setMontoTotalExitoso(BigDecimal montoTotalExitoso) {
        this.montoTotalExitoso = montoTotalExitoso;
        calcularTasaConversion();
    }
    
    public BigDecimal getMontoTotalFallido() {
        return montoTotalFallido;
    }
    
    public void setMontoTotalFallido(BigDecimal montoTotalFallido) {
        this.montoTotalFallido = montoTotalFallido;
    }
    
    public BigDecimal getTasaConversion() {
        return tasaConversion;
    }
    
    public void setTasaConversion(BigDecimal tasaConversion) {
        this.tasaConversion = tasaConversion;
    }
    
    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }
    
    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }
}