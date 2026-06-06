package com.minimarket.service.payment;

import com.minimarket.dto.mercadopago.ItemMPDTO;
import com.minimarket.dto.mercadopago.MercadoPagoRequestDTO;
import com.minimarket.dto.mercadopago.MercadoPagoResponseDTO;
import com.minimarket.dto.pago.PaymentContext;
import com.minimarket.dto.pago.PaymentResult;
import com.minimarket.dto.venta.FinalizarVentaRequest;
import com.minimarket.dto.venta.ItemVentaRequest;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.TransaccionPago;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.entity.enums.EstadoVenta;
import com.minimarket.entity.enums.MetodoPagoEnum;
import com.minimarket.repository.*;
import com.minimarket.service.mercadopago.MercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MercadoPagoPaymentProcessor implements PaymentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoPaymentProcessor.class);
    private static final Set<MetodoPagoEnum> METODOS_SOPORTADOS = Set.of(
            MetodoPagoEnum.MERCADO_PAGO);

    private final VentaRepository ventaRepo;
    private final DetalleVentaRepository detalleRepo;
    private final ProductoRepository productoRepo;
    private final TransaccionPagoRepository transaccionRepo;
    private final MercadoPagoService mercadoPagoService;

    public MercadoPagoPaymentProcessor(
            VentaRepository ventaRepo,
            DetalleVentaRepository detalleRepo,
            ProductoRepository productoRepo,
            TransaccionPagoRepository transaccionRepo,
            MercadoPagoService mercadoPagoService) {
        this.ventaRepo = ventaRepo;
        this.detalleRepo = detalleRepo;
        this.productoRepo = productoRepo;
        this.transaccionRepo = transaccionRepo;
        this.mercadoPagoService = mercadoPagoService;
    }

    @Override
    @Transactional
    public PaymentResult process(PaymentContext context) {
        logger.info("Procesando pago con Mercado Pago");

        FinalizarVentaRequest request = context.getRequest();
        Usuario vendedor = context.getVendedor();

        try {
            // 1. Validar items
            validarItems(request);

            // 2. Crear venta (sin descontar stock aún)
            Venta venta = crearVenta(vendedor);
            context.setVenta(venta);

            // 3. Procesar items y calcular total (sin descontar stock)
            BigDecimal total = procesarItems(venta, request.getItems());
            context.setTotalCalculado(total);

            // 4. Actualizar venta
            venta.setTotal(total);
            venta = ventaRepo.save(venta);
            context.setVenta(venta);

            // 5. Construir request para Mercado Pago
            MercadoPagoRequestDTO mpRequest = construirMercadoPagoRequest(venta, request, context);

            // 6. Llamar a Mercado Pago para crear preferencia
            MercadoPagoResponseDTO mpResponse = mercadoPagoService.crearPreferencia(
                    mpRequest,
                    context.getIpCliente(),
                    context.getUserAgent());

            // 7. Obtener la transacción creada
            TransaccionPago transaccion = transaccionRepo.findFirstByVentaOrderByFechaCreacionDesc(venta)
                    .orElseThrow(() -> new RuntimeException("Error al crear transacción"));

            logger.info("Preferencia creada para venta ID: {}, Preference ID: {}",
                    venta.getId(), mpResponse.getPreferenceId());

            // 8. Retornar resultado con URL de redirección
            return PaymentResult.pendiente(venta, transaccion, mpResponse.getInitPoint());

        } catch (Exception e) {
            logger.error("Error procesando pago con Mercado Pago: {}", e.getMessage(), e);

            // Si ya hay una venta, actualizar estado
            if (context.getVenta() != null) {
                context.getVenta().rechazarPago();
                ventaRepo.save(context.getVenta());
            }

            throw new RuntimeException("Error al procesar pago con Mercado Pago: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(MetodoPagoEnum metodoPago) {
        return METODOS_SOPORTADOS.contains(metodoPago);
    }

    @Override
    public String getProcessorName() {
        return "mercadopago-processor";
    }

    @Override
    public boolean isInmediato() {
        return false; // Mercado Pago es diferido (requiere redirección)
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarItems(FinalizarVentaRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }

        // Validar que todos los productos existen y tienen stock
        for (ItemVentaRequest item : request.getItems()) {
            if (item.getProductoId() == null) {
                throw new RuntimeException("ID de producto no puede ser nulo");
            }

            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para: " + producto.getNombre() +
                                ". Disponible: " + producto.getStock());
            }
        }
    }

    private Venta crearVenta(Usuario vendedor) {
        Venta venta = new Venta(vendedor);
        venta.setEstado(EstadoVenta.PENDIENTE);
        return ventaRepo.save(venta);
    }

    private BigDecimal procesarItems(Venta venta, List<ItemVentaRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVentaRequest item : items) {
            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // NO descontamos stock aún (solo validamos que hay)
            BigDecimal precio = producto.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precio);

            detalles.add(detalle);
        }

        detalleRepo.saveAll(detalles);
        venta.setDetalles(detalles);

        return total;
    }

    private MercadoPagoRequestDTO construirMercadoPagoRequest(
            Venta venta,
            FinalizarVentaRequest request,
            PaymentContext context) {

        MercadoPagoRequestDTO mpRequest = new MercadoPagoRequestDTO();
        mpRequest.setVentaId(venta.getId());

        // Datos del comprador (podrías obtenerlos del request si los envías)
        mpRequest.setEmailComprador("comprador@email.com"); // TODO: Obtener del request
        mpRequest.setNombreComprador(context.getVendedor().getUsername());

        // Convertir items al formato de Mercado Pago
        List<ItemMPDTO> itemsMP = request.getItems().stream()
                .map(item -> {
                    Producto producto = productoRepo.findById(item.getProductoId()).get();
                    ItemMPDTO itemMP = new ItemMPDTO();
                    itemMP.setProductoId(item.getProductoId());
                    itemMP.setTitulo(producto.getNombre());
                    itemMP.setDescripcion(producto.getDescripcion());
                    itemMP.setCantidad(item.getCantidad());
                    itemMP.setPrecioUnitario(producto.getPrecio());
                    itemMP.setCategoria(producto.getCategoria());
                    itemMP.setImagenUrl(producto.getImagen());
                    return itemMP;
                })
                .collect(Collectors.toList());

        mpRequest.setItems(itemsMP);

        return mpRequest;
    }
}