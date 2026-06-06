package com.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minimarket.entity.enums.EstadoTransaccion;
import com.minimarket.entity.enums.MetodoPagoEnum;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion_pago", indexes = {
        @Index(name = "idx_transaccion_venta", columnList = "venta_id"),
        @Index(name = "idx_transaccion_estado", columnList = "estado_transaccion"),
        @Index(name = "idx_transaccion_fecha", columnList = "fecha_creacion"),
        @Index(name = "idx_transaccion_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_transaccion_preference_id", columnList = "preference_id")
})
public class TransaccionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPagoEnum metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_transaccion", nullable = false, length = 20)
    private EstadoTransaccion estadoTransaccion;

    // IDs externos (para Mercado Pago u otros gateways)
    @Column(name = "transaction_id")
    private String transactionId; // ID del pago en el gateway (MP payment ID)

    @Column(name = "preference_id")
    private String preferenceId; // ID de preferencia (Mercado Pago)

    @Column(name = "init_point", length = 500)
    private String initPoint; // URL de pago (Mercado Pago)

    @Column(name = "payment_method_id", length = 50)
    private String paymentMethodId; // ID del método usado (visa, master, etc)

    @Column(name = "payment_type_id", length = 50)
    private String paymentTypeId; // Tipo (credit_card, debit_card, ticket)

    @Column(name = "payer_email", length = 100)
    private String payerEmail; // Email del pagador

    @Column(name = "payer_identification_type", length = 20)
    private String payerIdentificationType; // Tipo de documento (DNI, RUC)

    @Column(name = "payer_identification_number", length = 20)
    private String payerIdentificationNumber; // Número de documento

    @Column(name = "external_reference", length = 100)
    private String externalReference; // Referencia externa (ej: número de venta)

    // Metadatos adicionales (JSON)
    @Column(length = 2000)
    private String metadata; // Información adicional del gateway

    // Fechas de control
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion; // Para pagos con tiempo límite

    // Relación con el pago confirmado (tu entidad Pago actual)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pagoConfirmado;

    // Información del cliente para trazabilidad
    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "dispositivo", length = 50)
    private String dispositivo;

    // Intentos y errores
    @Column(name = "intento_numero")
    private Integer intentoNumero = 1;

    @Column(name = "codigo_error", length = 50)
    private String codigoError;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;

    @Column(name = "detalle_error", length = 1000)
    private String detalleError;

    // Auditoría
    @Column(name = "creado_por", length = 50)
    private String creadoPor;

    @Column(name = "actualizado_por", length = 50)
    private String actualizadoPor;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (fechaExpiracion == null) {
            fechaExpiracion = LocalDateTime.now().plusMinutes(30); // 30 min por defecto
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPagoEnum getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPagoEnum metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoTransaccion getEstadoTransaccion() {
        return estadoTransaccion;
    }

    public void setEstadoTransaccion(EstadoTransaccion estadoTransaccion) {
        this.estadoTransaccion = estadoTransaccion;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getPayerIdentificationType() {
        return payerIdentificationType;
    }

    public void setPayerIdentificationType(String payerIdentificationType) {
        this.payerIdentificationType = payerIdentificationType;
    }

    public String getPayerIdentificationNumber() {
        return payerIdentificationNumber;
    }

    public void setPayerIdentificationNumber(String payerIdentificationNumber) {
        this.payerIdentificationNumber = payerIdentificationNumber;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Pago getPagoConfirmado() {
        return pagoConfirmado;
    }

    public void setPagoConfirmado(Pago pagoConfirmado) {
        this.pagoConfirmado = pagoConfirmado;
    }

    public String getIpCliente() {
        return ipCliente;
    }

    public void setIpCliente(String ipCliente) {
        this.ipCliente = ipCliente;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public Integer getIntentoNumero() {
        return intentoNumero;
    }

    public void setIntentoNumero(Integer intentoNumero) {
        this.intentoNumero = intentoNumero;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public String getDetalleError() {
        return detalleError;
    }

    public void setDetalleError(String detalleError) {
        this.detalleError = detalleError;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }

    // Métodos de utilidad
    public boolean isExpirada() {
        return fechaExpiracion != null && LocalDateTime.now().isAfter(fechaExpiracion);
    }

    public boolean isConfirmada() {
        return estadoTransaccion == EstadoTransaccion.EXITOSO && fechaConfirmacion != null;
    }

    public boolean isFallida() {
        return estadoTransaccion == EstadoTransaccion.FALLIDO;
    }

    public boolean isReintentable() {
        return estadoTransaccion.isReintentable() && !isExpirada() && intentoNumero < 3;
    }

    @Override
    public String toString() {
        return "TransaccionPago{" +
                "id=" + id +
                ", metodoPago=" + metodoPago +
                ", estadoTransaccion=" + estadoTransaccion +
                ", monto=" + monto +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}