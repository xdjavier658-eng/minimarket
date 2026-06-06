package com.minimarket.service.payment;

import com.minimarket.dto.pago.PaymentContext;
import com.minimarket.dto.pago.PaymentResult;
import com.minimarket.entity.enums.MetodoPagoEnum;

/**
 * Interfaz para todos los procesadores de pago
 * Implementa el patrón Strategy
 */
public interface PaymentProcessor {
    
    /**
     * Procesa un pago según el método específico
     * @param context Contexto completo del pago
     * @return Resultado del procesamiento
     */
    PaymentResult process(PaymentContext context);
    
    /**
     * Indica si este procesador soporta el método de pago
     * @param metodoPago Método de pago a verificar
     * @return true si soporta el método
     */
    boolean supports(MetodoPagoEnum metodoPago);
    
    /**
     * Obtiene el nombre del procesador
     */
    String getProcessorName();
    
    /**
     * Indica si el procesador maneja pagos inmediatos (efectivo)
     * o pagos diferidos (Mercado Pago)
     */
    boolean isInmediato();
}