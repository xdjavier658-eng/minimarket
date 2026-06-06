package com.minimarket.entity.enums;

/**
 * Estados posibles de una venta en el sistema
 */
public enum EstadoVenta {
    
    PENDIENTE("Pendiente", "Venta iniciada, en proceso de creación"),
    PENDIENTE_PAGO("Pendiente de Pago", "Esperando confirmación de pago"),  // ← Estado actual del sistema
    EN_PROCESO("En Proceso de Pago", "Pago en línea en curso"),             // ← Nuevo para Mercado Pago
    PAGADO("Pagado", "Venta completada y pagada"),                           // ← Estado actual
    RECHAZADO("Rechazado", "Pago rechazado por el gateway"),                 // ← Nuevo
    CANCELADO("Cancelado", "Venta cancelada por el usuario"),                // ← Nuevo
    EXPIRADO("Expirado", "Tiempo de pago expirado");                         // ← Nuevo
    
    private final String nombre;
    private final String descripcion;
    
    EstadoVenta(String nombre, String descripcion) {
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
     * Verifica si el estado permite modificar la venta
     */
    public boolean isEditable() {
        return this == PENDIENTE || this == PENDIENTE_PAGO;
    }
    
    /**
     * Verifica si el estado es un estado final (no se puede cambiar)
     */
    public boolean isFinal() {
        return this == PAGADO || this == RECHAZADO || this == CANCELADO || this == EXPIRADO;
    }
    
    /**
     * Verifica si el estado requiere acción del usuario
     */
    public boolean requiresUserAction() {
        return this == PENDIENTE_PAGO || this == EN_PROCESO;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}