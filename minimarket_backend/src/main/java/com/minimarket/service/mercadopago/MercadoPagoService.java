package com.minimarket.service.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import com.minimarket.dto.mercadopago.ItemMPDTO;
import com.minimarket.dto.mercadopago.MercadoPagoRequestDTO;
import com.minimarket.dto.mercadopago.MercadoPagoResponseDTO;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.Pago;
import com.minimarket.entity.Producto;
import com.minimarket.entity.TransaccionPago;
import com.minimarket.entity.Venta;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.enums.EstadoTransaccion;
import com.minimarket.repository.MetodoPagoRepository;
import com.minimarket.repository.PagoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.TransaccionPagoRepository;
import com.minimarket.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.success-url}")
    private String successUrl;

    @Value("${mercadopago.failure-url}")
    private String failureUrl;

    @Value("${mercadopago.pending-url}")
    private String pendingUrl;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @Value("${mercadopago.expiration-minutes:30}")
    private Integer expirationMinutes;

    @Value("${mercadopago.sandbox:true}")
    private Boolean sandbox;

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final TransaccionPagoRepository transaccionRepo;
    private final PagoRepository pagoRepo;
    private final MetodoPagoRepository metodoPagoRepo;

    public MercadoPagoService(
            VentaRepository ventaRepo,
            ProductoRepository productoRepo,
            TransaccionPagoRepository transaccionRepo,
            PagoRepository pagoRepo,
            MetodoPagoRepository metodoPagoRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.transaccionRepo = transaccionRepo;
        this.pagoRepo = pagoRepo;
        this.metodoPagoRepo = metodoPagoRepo;
    }

    /**
     * Crear preferencia de pago en Mercado Pago
     */
    @Transactional
    public MercadoPagoResponseDTO crearPreferencia(MercadoPagoRequestDTO request, String ipCliente, String userAgent) {
        logger.info("Creando preferencia de pago para venta ID: {}", request.getVentaId());

        try {
            // 1. Configurar SDK
            MercadoPagoConfig.setAccessToken(accessToken);

            // 2. Obtener venta
            Venta venta = ventaRepo.findById(request.getVentaId())
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + request.getVentaId()));

            // 3. Validar que la venta esté pendiente
            if (!venta.isPendiente()) {
                throw new RuntimeException("La venta no está en estado pendiente. Estado actual: " + venta.getEstado());
            }

            // 4. Crear transacción en BD
            TransaccionPago transaccion = new TransaccionPago();
            transaccion.setVenta(venta);
            transaccion.setMonto(venta.getTotal());
            transaccion.setMetodoPago(com.minimarket.entity.enums.MetodoPagoEnum.MERCADO_PAGO);
            transaccion.setEstadoTransaccion(EstadoTransaccion.INICIADO);
            transaccion.setIpCliente(ipCliente);
            transaccion.setUserAgent(userAgent);
            transaccion.setPayerEmail(request.getEmailComprador());
            transaccion.setExternalReference(venta.getId().toString());
            transaccion.setFechaExpiracion(LocalDateTime.now().plusMinutes(expirationMinutes));

            if (request.getDocumentoType() != null && request.getDocumentoNumero() != null) {
                transaccion.setPayerIdentificationType(request.getDocumentoType());
                transaccion.setPayerIdentificationNumber(request.getDocumentoNumero());
            }

            transaccion = transaccionRepo.save(transaccion);

            // 5. Actualizar estado de venta
            venta.procesarPagoOnline();
            venta.addTransaccion(transaccion);
            ventaRepo.save(venta);

            // 6. Construir items para Mercado Pago
            List<PreferenceItemRequest> items = construirItems(request.getItems(), venta);

            // 7. Configurar pagador
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(request.getEmailComprador())
                    .name(request.getNombreComprador())
                    .build();

            // 8. Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl + "?ventaId=" + venta.getId() + "&transaccionId=" + transaccion.getId())
                    .failure(failureUrl + "?ventaId=" + venta.getId() + "&transaccionId=" + transaccion.getId())
                    .pending(pendingUrl + "?ventaId=" + venta.getId() + "&transaccionId=" + transaccion.getId())
                    .build();

            // 9. Configurar metadata
            java.util.HashMap<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("venta_id", venta.getId());
            metadata.put("transaccion_id", transaccion.getId());
            metadata.put("vendedor", venta.getVendedor().getUsername());

            // 10. Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .notificationUrl(notificationUrl)
                    .externalReference(venta.getId().toString())
                    .metadata(metadata)
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now(ZoneId.systemDefault()))
                    .expirationDateTo(
                            transaccion.getFechaExpiracion().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 11. Actualizar transacción con datos de MP (preferenceId, NO transactionId)
            transaccion.setPreferenceId(preference.getId());
            transaccion.setInitPoint(preference.getInitPoint());
            transaccion.setMetadata("preferenceId=" + preference.getId());
            transaccionRepo.save(transaccion);

            // 12. Construir respuesta
            MercadoPagoResponseDTO response = new MercadoPagoResponseDTO();
            response.setPreferenceId(preference.getId());
            // En sandbox usar sandboxInitPoint para el simulador
            response.setInitPoint(
                    Boolean.TRUE.equals(sandbox) ? preference.getSandboxInitPoint() : preference.getInitPoint());
            response.setSandboxInitPoint(preference.getSandboxInitPoint());
            response.setTransactionId(preference.getId()); // preferenceId como referencia inicial
            response.setVentaId(venta.getId());
            response.setMonto(venta.getTotal());
            response.setEstado(venta.getEstado().toString());
            response.setFechaExpiracion(transaccion.getFechaExpiracion()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            logger.info("Preferencia creada exitosamente para venta ID: {}. PreferenceID: {}. sandboxInitPoint: {}",
                    venta.getId(), preference.getId(), preference.getSandboxInitPoint());

            return response;

        } catch (Exception e) {
            logger.error("Error creando preferencia de pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener información de un pago desde Mercado Pago
     */
    public Payment obtenerPago(String paymentId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            return client.get(Long.parseLong(paymentId));
        } catch (Exception e) {
            logger.error("Error obteniendo pago {}: {}", paymentId, e.getMessage());
            return null;
        }
    }

    /**
     * Procesar notificación webhook de Mercado Pago
     * MP llama a este endpoint cuando hay cambios en el estado del pago
     */
    @Transactional
    public void procesarNotificacion(String paymentId, String topic) {
        logger.info("Procesando notificación webhook - PaymentID: {}, Topic: {}", paymentId, topic);

        try {
            if (!"payment".equals(topic)) {
                logger.info("Topic no manejado: {}. Solo se procesan topics 'payment'", topic);
                return;
            }

            // Obtener información del pago desde MP
            Payment payment = obtenerPago(paymentId);
            if (payment == null) {
                logger.error("No se pudo obtener información del pago: {}", paymentId);
                return;
            }

            // Buscar transacción por external reference (venta ID)
            String externalReference = payment.getExternalReference();
            if (externalReference == null || externalReference.isBlank()) {
                logger.error("El pago {} no tiene externalReference", paymentId);
                return;
            }

            Long ventaId = Long.parseLong(externalReference);

            Venta venta = ventaRepo.findById(ventaId)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + ventaId));

            TransaccionPago transaccion = transaccionRepo.findFirstByVentaOrderByFechaCreacionDesc(venta)
                    .orElseThrow(() -> new RuntimeException("Transacción no encontrada para venta: " + ventaId));

            // Si ya está en estado final, no procesar de nuevo
            if (transaccion.getEstadoTransaccion().isFinal()) {
                logger.info("Transacción {} ya en estado final: {}. Se ignora notificación.",
                        transaccion.getId(), transaccion.getEstadoTransaccion());
                return;
            }

            // Actualizar IDs y metadata del pago
            String status = payment.getStatus();
            String statusDetail = payment.getStatusDetail();

            transaccion.setTransactionId(paymentId);
            if (payment.getPaymentMethodId() != null) {
                transaccion.setPaymentMethodId(payment.getPaymentMethodId());
            }
            if (payment.getPaymentTypeId() != null) {
                transaccion.setPaymentTypeId(payment.getPaymentTypeId());
            }
            transaccion.setFechaActualizacion(LocalDateTime.now());

            switch (status) {
                case "approved":
                    logger.info("Pago APROBADO para venta ID: {}. PaymentID: {}", ventaId, paymentId);
                    transaccion.setEstadoTransaccion(EstadoTransaccion.EXITOSO);
                    transaccion.setFechaConfirmacion(LocalDateTime.now());

                    // Descontar stock y crear registro Pago solo si la venta no está ya pagada
                    if (!venta.isPagada()) {
                        descontarStockYConfirmarPago(venta, transaccion, payment.getExternalReference());
                    }
                    break;

                case "rejected":
                    logger.warn("Pago RECHAZADO para venta ID: {}. Detalle: {}", ventaId, statusDetail);
                    transaccion.setEstadoTransaccion(EstadoTransaccion.FALLIDO);
                    transaccion.setCodigoError(statusDetail);
                    transaccion.setMensajeError("Pago rechazado: " + statusDetail);
                    // Solo marcar como rechazado si no está ya pagada
                    if (!venta.isPagada()) {
                        venta.rechazarPago();
                    }
                    break;

                case "pending":
                case "in_process":
                    logger.info("Pago PENDIENTE/EN_PROCESO para venta ID: {}", ventaId);
                    transaccion.setEstadoTransaccion(EstadoTransaccion.EN_PROCESO);
                    break;

                case "cancelled":
                    logger.info("Pago CANCELADO para venta ID: {}", ventaId);
                    transaccion.setEstadoTransaccion(EstadoTransaccion.FALLIDO);
                    transaccion.setMensajeError("Pago cancelado por el usuario");
                    if (!venta.isPagada()) {
                        venta.cancelar();
                    }
                    break;

                case "refunded":
                    logger.info("Pago REEMBOLSADO para venta ID: {}", ventaId);
                    transaccion.setEstadoTransaccion(EstadoTransaccion.REEMBOLSADO);
                    break;

                default:
                    logger.info("Estado MP no manejado: '{}' para venta ID: {}", status, ventaId);
                    transaccion.setEstadoTransaccion(EstadoTransaccion.EN_PROCESO);
            }

            transaccionRepo.save(transaccion);
            ventaRepo.save(venta);

            logger.info("Notificación procesada exitosamente para venta ID: {}. Nuevo estado: {}", ventaId,
                    transaccion.getEstadoTransaccion());

        } catch (Exception e) {
            logger.error("Error procesando notificación webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando notificación: " + e.getMessage(), e);
        }
    }

    /**
     * Confirmar pago desde el frontend cuando regresa de Mercado Pago.
     * Se llama cuando el usuario es redirigido de vuelta a la success URL.
     * Busca el pago por preferenceId y verifica el estado en MP.
     */
    @Transactional
    public MercadoPagoResponseDTO confirmarPagoPorVenta(Long ventaId) {
        logger.info("Confirmando pago por venta ID: {}", ventaId);

        try {
            Venta venta = ventaRepo.findById(ventaId)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + ventaId));

            TransaccionPago transaccion = transaccionRepo.findFirstByVentaOrderByFechaCreacionDesc(venta)
                    .orElseThrow(() -> new RuntimeException("Transacción no encontrada para venta: " + ventaId));

            MercadoPagoResponseDTO response = new MercadoPagoResponseDTO();
            response.setVentaId(ventaId);
            response.setMonto(transaccion.getMonto());
            response.setPreferenceId(transaccion.getPreferenceId());

            if (transaccion.getEstadoTransaccion() == EstadoTransaccion.EXITOSO || venta.isPagada()) {
                response.setEstado("PAGADO");
            } else if (transaccion.getEstadoTransaccion() == EstadoTransaccion.FALLIDO) {
                response.setEstado("RECHAZADO");
                response.setTransactionId(transaccion.getTransactionId());
            } else {
                // Estado en proceso — devolver estado actual
                response.setEstado(transaccion.getEstadoTransaccion().toString());
            }

            if (transaccion.getTransactionId() != null) {
                response.setTransactionId(transaccion.getTransactionId());
            }

            return response;

        } catch (Exception e) {
            logger.error("Error confirmando pago para venta {}: {}", ventaId, e.getMessage());
            throw new RuntimeException("Error al confirmar pago: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar estado de una transacción por su transactionId o preferenceId
     */
    public MercadoPagoResponseDTO verificarEstado(String transactionId) {
        try {
            // Primero buscar por transactionId (paymentId), si no por preferenceId
            TransaccionPago transaccion = transaccionRepo.findByTransactionId(transactionId)
                    .or(() -> transaccionRepo.findByPreferenceId(transactionId))
                    .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transactionId));

            MercadoPagoResponseDTO response = new MercadoPagoResponseDTO();

            // Usar transactionId si existe, sino el preferenceId como referencia
            String txId = transaccion.getTransactionId() != null
                    ? transaccion.getTransactionId()
                    : transaccion.getPreferenceId();
            response.setTransactionId(txId);

            response.setVentaId(transaccion.getVenta().getId());
            response.setMonto(transaccion.getMonto());
            response.setEstado(transaccion.getEstadoTransaccion().toString());
            response.setPreferenceId(transaccion.getPreferenceId());

            if (transaccion.getFechaExpiracion() != null) {
                response.setFechaExpiracion(transaccion.getFechaExpiracion()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            return response;

        } catch (Exception e) {
            logger.error("Error verificando estado: {}", e.getMessage());
            throw new RuntimeException("Error verificando estado: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    /**
     * Descuenta el stock de los productos y crea el registro Pago confirmado
     */
    private void descontarStockYConfirmarPago(Venta venta, TransaccionPago transaccion, String referencia) {
        logger.info("Descontando stock y confirmando pago para venta ID: {}", venta.getId());

        try {
            // Descontar stock de cada producto
            List<DetalleVenta> detalles = venta.getDetalles();
            if (detalles == null || detalles.isEmpty()) {
                logger.warn("La venta ID: {} no tiene detalles. No se descuenta stock.", venta.getId());
            } else {
                for (DetalleVenta detalle : detalles) {
                    Producto producto = detalle.getProducto();
                    int nuevoStock = producto.getStock() - detalle.getCantidad();

                    if (nuevoStock < 0) {
                        logger.error("Stock insuficiente para producto '{}'. Stock actual: {}, cantidad solicitada: {}",
                                producto.getNombre(), producto.getStock(), detalle.getCantidad());
                        // En producción deberías lanzar excepción o manejar reembolso automatico
                        // Por ahora dejamos el stock en 0 y logueamos el error
                        nuevoStock = 0;
                    }

                    producto.setStock(nuevoStock);
                    productoRepo.save(producto);
                    logger.debug("Stock actualizado: Producto '{}' → nuevo stock: {}",
                            producto.getNombre(), nuevoStock);
                }
            }

            // Obtener el método de pago MERCADO_PAGO de la BD
            MetodoPago metodoPagoDB = metodoPagoRepo.findByNombre("MERCADO_PAGO")
                    .orElse(null);

            // Crear registro Pago confirmado
            Pago pago = new Pago();
            pago.setVenta(venta);
            pago.setMonto(venta.getTotal());
            pago.setEstado("CONFIRMADO");
            pago.setFecha(LocalDateTime.now());
            pago.setReferencia(transaccion.getTransactionId() != null
                    ? transaccion.getTransactionId()
                    : transaccion.getPreferenceId());

            if (metodoPagoDB != null) {
                pago.setMetodoPago(metodoPagoDB);
            }

            pagoRepo.save(pago);

            // Confirmar la venta
            venta.confirmarPago(pago);
            ventaRepo.save(venta);

            logger.info("Pago confirmado para venta ID: {}. Stock descontado.", venta.getId());

        } catch (Exception e) {
            logger.error("Error al descontar stock o confirmar pago para venta {}: {}", venta.getId(), e.getMessage());
            throw new RuntimeException("Error al confirmar pago: " + e.getMessage(), e);
        }
    }

    private List<PreferenceItemRequest> construirItems(List<ItemMPDTO> itemsDTO, Venta venta) {
        List<PreferenceItemRequest> items = new ArrayList<>();

        if (itemsDTO != null && !itemsDTO.isEmpty()) {
            // Usar items enviados desde el frontend
            for (ItemMPDTO itemDTO : itemsDTO) {
                Producto producto = productoRepo.findById(itemDTO.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDTO.getProductoId()));

                // Validar stock
                if (producto.getStock() < itemDTO.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()
                            + ". Disponible: " + producto.getStock());
                }

                PreferenceItemRequest item = PreferenceItemRequest.builder()
                        .id(producto.getId().toString())
                        .title(itemDTO.getTitulo() != null ? itemDTO.getTitulo() : producto.getNombre())
                        .description(
                                itemDTO.getDescripcion() != null ? itemDTO.getDescripcion() : producto.getDescripcion())
                        .quantity(itemDTO.getCantidad())
                        .unitPrice(itemDTO.getPrecioUnitario())
                        .currencyId("PEN")
                        .build();

                items.add(item);
            }
        } else {
            // Construir desde los detalles de la venta
            venta.getDetalles().forEach(detalle -> {
                PreferenceItemRequest item = PreferenceItemRequest.builder()
                        .id(detalle.getProducto().getId().toString())
                        .title(detalle.getProducto().getNombre())
                        .quantity(detalle.getCantidad())
                        .unitPrice(detalle.getPrecioUnitario())
                        .currencyId("PEN")
                        .build();

                items.add(item);
            });
        }

        return items;
    }
}