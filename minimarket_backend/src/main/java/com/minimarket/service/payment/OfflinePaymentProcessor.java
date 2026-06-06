package com.minimarket.service.payment;

import com.minimarket.dto.pago.PaymentContext;
import com.minimarket.dto.pago.PaymentResult;
import com.minimarket.dto.venta.FinalizarVentaRequest;
import com.minimarket.dto.venta.ItemVentaRequest;
import com.minimarket.entity.*;
import com.minimarket.entity.enums.EstadoTransaccion;
import com.minimarket.entity.enums.EstadoVenta;
import com.minimarket.entity.enums.MetodoPagoEnum;
import com.minimarket.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OfflinePaymentProcessor implements PaymentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OfflinePaymentProcessor.class);
    private static final Set<MetodoPagoEnum> METODOS_SOPORTADOS = Set.of(
            MetodoPagoEnum.EFECTIVO,
            MetodoPagoEnum.YAPE,
            MetodoPagoEnum.PLIN);

    private final VentaRepository ventaRepo;
    private final DetalleVentaRepository detalleRepo;
    private final PagoRepository pagoRepo;
    private final MetodoPagoRepository metodoPagoRepo;
    private final ProductoRepository productoRepo;
    private final TransaccionPagoRepository transaccionRepo;

    public OfflinePaymentProcessor(
            VentaRepository ventaRepo,
            DetalleVentaRepository detalleRepo,
            PagoRepository pagoRepo,
            MetodoPagoRepository metodoPagoRepo,
            ProductoRepository productoRepo,
            TransaccionPagoRepository transaccionRepo) {
        this.ventaRepo = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.pagoRepo = pagoRepo;
        this.metodoPagoRepo = metodoPagoRepo;
        this.productoRepo = productoRepo;
        this.transaccionRepo = transaccionRepo;
    }

    @Override
    @Transactional
    public PaymentResult process(PaymentContext context) {
        logger.info("Procesando pago offline para método: {}", context.getMetodoPagoEnum());

        FinalizarVentaRequest request = context.getRequest();
        Usuario vendedor = context.getVendedor();

        try {
            // 1. Validaciones específicas para métodos offline
            validarMetodoPago(request, context.getMetodoPagoEnum());
            validarItems(request);

            // 2. Crear venta primero (necesitamos el ID de venta para la transacción)
            Venta venta = crearVenta(vendedor);
            context.setVenta(venta);

            // 3. Crear transacción de pago vinculada a la venta (venta_id NOT NULL)
            TransaccionPago transaccion = crearTransaccion(context, venta);

            // 4. Procesar items y descontar stock (para offline se descuenta inmediato)
            BigDecimal total = procesarItemsYDescontarStock(venta, request.getItems());
            context.setTotalCalculado(total);

            // 5. Actualizar venta con total, estado y transacción
            venta.setTotal(total);
            venta.iniciarPago(); // PENDIENTE_PAGO
            venta.addTransaccion(transaccion);
            venta = ventaRepo.save(venta);
            context.setVenta(venta);

            // 6. Crear pago confirmado (para offline es inmediato)
            Pago pago = crearPagoConfirmado(venta, request, context.getMetodoPagoEnum());
            venta.confirmarPago(pago);

            // 7. Actualizar transacción a EXITOSO
            transaccion.setEstadoTransaccion(EstadoTransaccion.EXITOSO);
            transaccion.setFechaConfirmacion(LocalDateTime.now());
            transaccion.setPagoConfirmado(pago);
            transaccionRepo.save(transaccion);

            // 8. Guardar venta final con pago confirmado
            venta = ventaRepo.save(venta);

            logger.info("Pago offline procesado exitosamente para venta ID: {}", venta.getId());

            return PaymentResult.exito(venta, pago, transaccion);

        } catch (Exception e) {
            logger.error("Error procesando pago offline: {}", e.getMessage(), e);

            // Si ya hay una venta creada, marcar su última transacción como fallida
            if (context.getVenta() != null) {
                TransaccionPago transaccion = context.getVenta().getUltimaTransaccion();
                if (transaccion != null) {
                    transaccion.setEstadoTransaccion(EstadoTransaccion.FALLIDO);
                    transaccion.setMensajeError(e.getMessage());
                    transaccionRepo.save(transaccion);
                }
            }

            throw new RuntimeException("Error procesando pago: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(MetodoPagoEnum metodoPago) {
        return METODOS_SOPORTADOS.contains(metodoPago);
    }

    @Override
    public String getProcessorName() {
        return "offline-processor";
    }

    @Override
    public boolean isInmediato() {
        return true; // Los pagos offline son inmediatos
    }

    // ========== MÉTODOS PRIVADOS (código existente adaptado) ==========

    private void validarMetodoPago(FinalizarVentaRequest request, MetodoPagoEnum metodoPagoEnum) {
        if (request.getMetodoPagoId() == null) {
            throw new RuntimeException("ID de método de pago no puede ser nulo");
        }

        MetodoPago metodoPago = metodoPagoRepo.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new RuntimeException("Método de pago no existe"));

        if (!metodoPago.getActivo()) {
            throw new RuntimeException("Método de pago no disponible");
        }

        // Validación específica para YAPE/PLIN (requieren referencia)
        if ((metodoPagoEnum == MetodoPagoEnum.YAPE || metodoPagoEnum == MetodoPagoEnum.PLIN)
                && (request.getReferencia() == null || request.getReferencia().isBlank())) {
            throw new RuntimeException("Referencia obligatoria para " + metodoPagoEnum.getNombre());
        }
    }

    private void validarItems(FinalizarVentaRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }
    }

    private TransaccionPago crearTransaccion(PaymentContext context, Venta venta) {
        TransaccionPago transaccion = new TransaccionPago();
        transaccion.setVenta(venta); // ← venta_id ya tiene valor
        transaccion.setMonto(BigDecimal.ZERO); // se actualizará después
        transaccion.setMetodoPago(context.getMetodoPagoEnum());
        transaccion.setEstadoTransaccion(EstadoTransaccion.INICIADO);
        transaccion.setIpCliente(context.getIpCliente());
        transaccion.setUserAgent(context.getUserAgent());
        transaccion.setCreadoPor(context.getVendedor().getUsername());
        transaccion.setIntentoNumero(1);

        return transaccionRepo.save(transaccion);
    }

    private Venta crearVenta(Usuario vendedor) {
        Venta venta = new Venta(vendedor);
        return ventaRepo.save(venta);
    }

    private BigDecimal procesarItemsYDescontarStock(Venta venta, List<ItemVentaRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVentaRequest item : items) {
            if (item.getProductoId() == null) {
                throw new RuntimeException("ID de producto no puede ser nulo");
            }

            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Integer cantidadSolicitada = item.getCantidad();

            if (cantidadSolicitada == null || cantidadSolicitada <= 0) {
                throw new RuntimeException("Cantidad inválida para producto: " + producto.getNombre());
            }

            // Validar stock
            if (producto.getStock() < cantidadSolicitada) {
                throw new RuntimeException(
                        "Stock insuficiente para el producto: " + producto.getNombre()
                                + ". Disponible: " + producto.getStock());
            }

            // Descontar stock (inmediato para offline)
            producto.setStock(producto.getStock() - cantidadSolicitada);
            productoRepo.save(producto);

            BigDecimal precio = producto.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidadSolicitada));
            total = total.add(subtotal);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadSolicitada);
            detalle.setPrecioUnitario(precio);

            detalles.add(detalle);
        }

        detalleRepo.saveAll(detalles);
        return total;
    }

    private Pago crearPagoConfirmado(Venta venta, FinalizarVentaRequest request, MetodoPagoEnum metodoPagoEnum) {
        MetodoPago metodoPago = metodoPagoRepo.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new RuntimeException("Método de pago no existe"));

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(venta.getTotal());
        pago.setEstado("CONFIRMADO");
        pago.setReferencia(request.getReferencia());
        pago.setFecha(LocalDateTime.now());

        return pagoRepo.save(pago);
    }
}