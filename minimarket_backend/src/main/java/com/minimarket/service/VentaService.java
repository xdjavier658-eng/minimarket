package com.minimarket.service;

import com.minimarket.dto.venta.FinalizarVentaRequest;
import com.minimarket.dto.venta.FinalizarVentaResponse;
import com.minimarket.entity.*;
import com.minimarket.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepo;
    private final DetalleVentaRepository detalleRepo;
    private final PagoRepository pagoRepo;
    private final MetodoPagoRepository metodoPagoRepo;
    private final ProductoRepository productoRepo;

    public VentaService(
            VentaRepository ventaRepo,
            DetalleVentaRepository detalleRepo,
            PagoRepository pagoRepo,
            MetodoPagoRepository metodoPagoRepo,
            ProductoRepository productoRepo) {
        this.ventaRepo = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.pagoRepo = pagoRepo;
        this.metodoPagoRepo = metodoPagoRepo;
        this.productoRepo = productoRepo;
    }

    @Transactional
    public FinalizarVentaResponse finalizarVenta(
            FinalizarVentaRequest request,
            Usuario vendedor) {

        if (request.getMetodoPagoId() == null) {
            throw new RuntimeException("ID de método de pago no puede ser nulo");
        }

        MetodoPago metodoPago = metodoPagoRepo.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new RuntimeException("Método de pago no existe"));

        if (!metodoPago.getActivo()) {
            throw new RuntimeException("Método de pago no disponible");
        }

        if (!metodoPago.getNombre().equalsIgnoreCase("EFECTIVO")
                && (request.getReferencia() == null || request.getReferencia().isBlank())) {
            throw new RuntimeException("Referencia obligatoria para YAPE o PLIN");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }

        Venta venta = new Venta();
        venta.setVendedor(vendedor);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado("PENDIENTE_PAGO");
        venta.setTotal(BigDecimal.ZERO);

        venta = ventaRepo.save(venta);

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (var item : request.getItems()) {

            if (item.getProductoId() == null) {
                throw new RuntimeException("ID de producto no puede ser nulo");
            }

            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Integer cantidadSolicitada = item.getCantidad();

            if (cantidadSolicitada == null || cantidadSolicitada <= 0) {
                throw new RuntimeException("Cantidad inválida para producto: " + producto.getNombre());
            }

            // 🔴 VALIDACIÓN DE STOCK
            if (producto.getStock() < cantidadSolicitada) {
                throw new RuntimeException(
                        "Stock insuficiente para el producto: " + producto.getNombre()
                                + ". Disponible: " + producto.getStock()
                );
            }

            // 🟢 DESCONTAR STOCK
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

        venta.setTotal(total);
        venta.setEstado("PAGADO");
        ventaRepo.save(venta);

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(total);
        pago.setEstado("CONFIRMADO");
        pago.setReferencia(request.getReferencia());
        pago.setFecha(LocalDateTime.now());

        pagoRepo.save(pago);

        FinalizarVentaResponse response = new FinalizarVentaResponse();
        response.setVentaId(venta.getId());
        response.setEstado(venta.getEstado());
        response.setTotal(total);
        response.setMetodoPago(metodoPago.getNombre());

        return response;
    }
}
