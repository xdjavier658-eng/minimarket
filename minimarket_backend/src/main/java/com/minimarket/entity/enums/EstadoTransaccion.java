package com.minimarket.entity.enums;

/**
 * Estados posibles de una transacción de pago
 * Una venta puede tener múltiples transacciones (intentos de pago)
 */
public enum EstadoTransaccion {
    
    INICIADO("Iniciado", "Transacción creada, esperando procesamiento"),
    EN_PROCESO("En Proceso", "Pago en curso con el gateway"),
    EXITOSO("Exitoso", "Pago confirmado exitosamente"),
    FALLIDO("Fallido", "Pago rechazado por el gateway"),
    EXPIRADO("Expirado", "Tiempo límite de pago excedido"),
    REEMBOLSADO("Reembolsado", "Pago reembolsado al cliente");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoTransaccion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Verifica si la transacción está en estado final
     */
    public boolean isFinal() {
        return this == EXITOSO || this == FALLIDO || this == EXPIRADO || this == REEMBOLSADO;
    }
    
    /**
     * Verifica si la transacción fue exitosa
     */
    public boolean isExitosa() {
        return this == EXITOSO;
    }
    
    /**
     * Verifica si se puede reintentar la transacción
     */
    public boolean isReintentable() {
        return this == FALLIDO || this == EXPIRADO;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}