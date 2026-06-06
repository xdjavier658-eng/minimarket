package com.minimarket.entity.enums;

/**
 * Métodos de pago soportados por el sistema
 * Incluye tanto métodos actuales como futuros
 */
public enum MetodoPagoEnum {
    
    EFECTIVO("Efectivo", "Pago en efectivo", false, true),
    YAPE("Yape", "Pago con Yape (BCP)", true, true),
    PLIN("Plin", "Pago con Plin (Interbank)", true, true),
    MERCADO_PAGO("Mercado Pago", "Pago con Mercado Pago (tarjeta/QR)", true, false);
    
    private final String nombre;
    private final String descripcion;
    private final boolean requiereReferencia;  // Si necesita número de operación
    private final boolean esInmediato;         // Si la confirmación es inmediata
    
    MetodoPagoEnum(String nombre, String descripcion, boolean requiereReferencia, boolean esInmediato) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.requiereReferencia = requiereReferencia;
        this.esInmediato = esInmediato;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public boolean isRequiereReferencia() {
        return requiereReferencia;
    }
    
    public boolean isEsInmediato() {
        return esInmediato;
    }
    
    /**
     * Verifica si el método de pago requiere integración con gateway externo
     */
    public boolean requiereGateway() {
        return this == MERCADO_PAGO;
    }
    
    /**
     * Obtiene el método de pago por su nombre
     */
    public static MetodoPagoEnum fromNombre(String nombre) {
        for (MetodoPagoEnum metodo : values()) {
            if (metodo.name().equalsIgnoreCase(nombre) || 
                metodo.getNombre().equalsIgnoreCase(nombre)) {
                return metodo;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}