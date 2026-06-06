package com.minimarket.service.payment;

import com.minimarket.entity.enums.MetodoPagoEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fábrica que proporciona el procesador adecuado según el método de pago
 */
@Component
public class PaymentProcessorFactory {
    
    private final Map<MetodoPagoEnum, PaymentProcessor> processorMap = new ConcurrentHashMap<>();
    
    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        // Registrar cada procesador con los métodos que soporta
        for (PaymentProcessor processor : processors) {
            for (MetodoPagoEnum metodo : MetodoPagoEnum.values()) {
                if (processor.supports(metodo)) {
                    processorMap.put(metodo, processor);
                }
            }
        }
    }
    
    /**
     * Obtiene el procesador adecuado para un método de pago
     * @param metodoPago Método de pago solicitado
     * @return Procesador correspondiente
     * @throws IllegalArgumentException si no hay procesador para el método
     */
    public PaymentProcessor getProcessor(MetodoPagoEnum metodoPago) {
        PaymentProcessor processor = processorMap.get(metodoPago);
        if (processor == null) {
            throw new IllegalArgumentException(
                "No hay procesador configurado para el método de pago: " + metodoPago
            );
        }
        return processor;
    }
    
    /**
     * Verifica si existe un procesador para el método
     */
    public boolean hasProcessor(MetodoPagoEnum metodoPago) {
        return processorMap.containsKey(metodoPago);
    }
    
    /**
     * Obtiene todos los métodos de pago disponibles
     */
    public List<MetodoPagoEnum> getMetodosDisponibles() {
        return List.copyOf(processorMap.keySet());
    }
    
    /**
     * Obtiene los métodos de pago inmediatos
     */
    public List<MetodoPagoEnum> getMetodosInmediatos() {
        return processorMap.entrySet().stream()
                .filter(entry -> entry.getValue().isInmediato())
                .map(Map.Entry::getKey)
                .toList();
    }
    
    /**
     * Obtiene los métodos de pago que requieren gateway
     */
    public List<MetodoPagoEnum> getMetodosConGateway() {
        return processorMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isInmediato())
                .map(Map.Entry::getKey)
                .toList();
    }
}