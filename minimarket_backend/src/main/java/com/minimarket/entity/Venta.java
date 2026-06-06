package com.minimarket.entity;

import com.minimarket.entity.enums.EstadoVenta;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;

    @OneToOne(mappedBy = "venta", cascade = CascadeType.ALL)
    private Pago pago;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransaccionPago> transacciones = new ArrayList<>();

    public Venta() {
        this.estado = EstadoVenta.PENDIENTE.getNombre();
        this.fecha = LocalDateTime.now();
    }

    public Venta(Usuario vendedor) {
        this();
        this.vendedor = vendedor;
    }

    public List<TransaccionPago> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<TransaccionPago> transacciones) {
        this.transacciones = transacciones;
    }

    public void addTransaccion(TransaccionPago transaccion) {
        if (transaccion != null) {
            transaccion.setVenta(this);
            this.transacciones.add(transaccion);
        }
    }

    public void iniciarPago() {
        setEstado(EstadoVenta.PENDIENTE_PAGO);
    }

    public TransaccionPago getUltimaTransaccion() {
        if (transacciones == null || transacciones.isEmpty()) {
            return null;
        }
        return transacciones.get(transacciones.size() - 1);
    }

    public boolean isPendiente() {
        return EstadoVenta.PENDIENTE.getNombre().equalsIgnoreCase(estado)
                || EstadoVenta.PENDIENTE_PAGO.getNombre().equalsIgnoreCase(estado);
    }

    public boolean isPagada() {
        return EstadoVenta.PAGADO.getNombre().equalsIgnoreCase(estado);
    }

    public void procesarPagoOnline() {
        setEstado(EstadoVenta.EN_PROCESO.getNombre());
    }

    public void rechazarPago() {
        setEstado(EstadoVenta.RECHAZADO.getNombre());
    }

    public void cancelar() {
        setEstado(EstadoVenta.CANCELADO.getNombre());
    }

    public void confirmarPago(Pago pago) {
        this.pago = pago;
        if (pago != null) {
            pago.setVenta(this);
        }
        setEstado(EstadoVenta.PAGADO.getNombre());
    }

    // getters y setters
    public Long getId() {
        return id;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public Pago getPago() {
        return pago;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado != null ? estado.getNombre() : null;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
