package com.minimarket.dto.pago;

import com.minimarket.entity.Pago;
import com.minimarket.entity.TransaccionPago;
import com.minimarket.entity.Venta;
import com.minimarket.entity.enums.EstadoVenta;

import java.math.BigDecimal;

/**
 * Resultado del procesamiento de un pago
 */
public class PaymentResult {
    
    private final boolean success;
    private final String message;
    private final Venta venta;
    private final Pago pago;
    private final TransaccionPago transaccion;
    private final String redirectUrl;      // Para pagos online (Mercado Pago)
    private final String transactionId;     // ID de la transacción externa
    private final EstadoVenta nuevoEstado;
    private final BigDecimal monto;
    
    private PaymentResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.venta = builder.venta;
        this.pago = builder.pago;
        this.transaccion = builder.transaccion;
        this.redirectUrl = builder.redirectUrl;
        this.transactionId = builder.transactionId;
        this.nuevoEstado = builder.nuevoEstado;
        this.monto = builder.monto;
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Venta getVenta() {
        return venta;
    }
    
    public Pago getPago() {
        return pago;
    }
    
    public TransaccionPago getTransaccion() {
        return transaccion;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public EstadoVenta getNuevoEstado() {
        return nuevoEstado;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    // Métodos de utilidad
    public boolean requiresRedirect() {
        return redirectUrl != null && !redirectUrl.isEmpty();
    }
    
    public boolean isPagoInmediato() {
        return pago != null;
    }
    
    // Builder
    public static class Builder {
        private boolean success;
        private String message;
        private Venta venta;
        private Pago pago;
        private TransaccionPago transaccion;
        private String redirectUrl;
        private String transactionId;
        private EstadoVenta nuevoEstado;
        private BigDecimal monto;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder venta(Venta venta) {
            this.venta = venta;
            return this;
        }
        
        public Builder pago(Pago pago) {
            this.pago = pago;
            return this;
        }
        
        public Builder transaccion(TransaccionPago transaccion) {
            this.transaccion = transaccion;
            return this;
        }
        
        public Builder redirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder nuevoEstado(EstadoVenta nuevoEstado) {
            this.nuevoEstado = nuevoEstado;
            return this;
        }
        
        public Builder monto(BigDecimal monto) {
            this.monto = monto;
            return this;
        }
        
        public PaymentResult build() {
            return new PaymentResult(this);
        }
    }
    
    // Factory methods
    public static PaymentResult exito(Venta venta, Pago pago, TransaccionPago transaccion) {
        return new Builder()
                .success(true)
                .message("Pago procesado exitosamente")
                .venta(venta)
                .pago(pago)
                .transaccion(transaccion)
                .nuevoEstado(EstadoVenta.PAGADO)
                .monto(venta.getTotal())
                .build();
    }
    
    public static PaymentResult pendiente(Venta venta, TransaccionPago transaccion, String redirectUrl) {
        return new Builder()
                .success(true)
                .message("Redirigiendo para completar pago")
                .venta(venta)
                .transaccion(transaccion)
                .redirectUrl(redirectUrl)
                .transactionId(transaccion.getTransactionId())
                .nuevoEstado(EstadoVenta.EN_PROCESO)
                .monto(venta.getTotal())
                .build();
    }
    
    public static PaymentResult error(String message, Venta venta, TransaccionPago transaccion) {
        return new Builder()
                .success(false)
                .message(message)
                .venta(venta)
                .transaccion(transaccion)
                .nuevoEstado(EstadoVenta.RECHAZADO)
                .build();
    }
}