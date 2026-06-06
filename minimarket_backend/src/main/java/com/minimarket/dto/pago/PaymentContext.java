package com.minimarket.dto.pago;

import com.minimarket.dto.venta.FinalizarVentaRequest;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.entity.enums.MetodoPagoEnum;

import java.math.BigDecimal;

/**
 * Contexto completo para procesar un pago
 * Contiene toda la información necesaria para cualquier método de pago
 */
public class PaymentContext {
    
    private final FinalizarVentaRequest request;
    private final Usuario vendedor;
    private final String ipCliente;
    private final String userAgent;
    
    // Datos que se van generando durante el proceso
    private Venta venta;
    private BigDecimal totalCalculado;
    private MetodoPagoEnum metodoPagoEnum;
    
    public PaymentContext(FinalizarVentaRequest request, Usuario vendedor, 
                         String ipCliente, String userAgent) {
        this.request = request;
        this.vendedor = vendedor;
        this.ipCliente = ipCliente;
        this.userAgent = userAgent;
    }
    
    // Getters
    public FinalizarVentaRequest getRequest() {
        return request;
    }
    
    public Usuario getVendedor() {
        return vendedor;
    }
    
    public String getIpCliente() {
        return ipCliente;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public Venta getVenta() {
        return venta;
    }
    
    public void setVenta(Venta venta) {
        this.venta = venta;
    }
    
    public BigDecimal getTotalCalculado() {
        return totalCalculado;
    }
    
    public void setTotalCalculado(BigDecimal totalCalculado) {
        this.totalCalculado = totalCalculado;
    }
    
    public MetodoPagoEnum getMetodoPagoEnum() {
        return metodoPagoEnum;
    }
    
    public void setMetodoPagoEnum(MetodoPagoEnum metodoPagoEnum) {
        this.metodoPagoEnum = metodoPagoEnum;
    }
}