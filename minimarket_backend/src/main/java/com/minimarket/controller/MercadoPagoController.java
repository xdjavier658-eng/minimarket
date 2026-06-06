package com.minimarket.controller;

import com.minimarket.dto.mercadopago.MercadoPagoRequestDTO;
import com.minimarket.dto.mercadopago.MercadoPagoResponseDTO;
import com.minimarket.dto.mercadopago.WebhookNotificationDTO;
import com.minimarket.service.mercadopago.MercadoPagoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
@CrossOrigin(origins = "http://localhost:8080")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);

    private final MercadoPagoService mercadoPagoService;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    /**
     * Endpoint para crear preferencia de pago
     * Este endpoint es llamado desde el frontend cuando el usuario elige pagar con
     * Mercado Pago
     */
    @PostMapping("/crear-preferencia")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MercadoPagoResponseDTO> crearPreferencia(
            @Valid @RequestBody MercadoPagoRequestDTO request,
            HttpServletRequest httpRequest) {

        logger.info("Solicitud para crear preferencia de pago para venta ID: {}", request.getVentaId());

        String ipCliente = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        MercadoPagoResponseDTO response = mercadoPagoService.crearPreferencia(
                request,
                ipCliente,
                userAgent);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar estado de una transacción
     */
    @GetMapping("/transaccion/{transactionId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'VENDEDOR')")
    public ResponseEntity<MercadoPagoResponseDTO> verificarEstado(
            @PathVariable String transactionId) {

        logger.info("Verificando estado de transacción: {}", transactionId);

        MercadoPagoResponseDTO response = mercadoPagoService.verificarEstado(transactionId);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para confirmar pago por venta ID.
     * El frontend llama a este endpoint cuando regresa del simulador de Mercado
     * Pago.
     * También puede ser llamado desde las páginas de éxito/fallo para saber el
     * estado real.
     */
    /**
     * Endpoint PÚBLICO para que el cliente consulte su estado de pago
     * al regresar del flujo de Mercado Pago. No requiere autenticación.
     */
    @GetMapping("/confirmar-pago/{ventaId}")
    public ResponseEntity<MercadoPagoResponseDTO> confirmarPago(
            @PathVariable Long ventaId) {

        logger.info("Consulta de confirmación de pago para venta ID: {}", ventaId);

        MercadoPagoResponseDTO response = mercadoPagoService.confirmarPagoPorVenta(ventaId);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint principal para webhook de Mercado Pago (IPN)
     * MP envía: POST /webhook?id=xxx&topic=payment (IPN clásico)
     * O bien: POST /webhook con body JSON (Webhooks v2)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirNotificacion(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "id", required = false) String id,
            @RequestBody(required = false) WebhookNotificationDTO body) {

        // IPN clásico usa: ?topic=payment&id=xxx
        // Webhooks v2 usa: ?type=payment&data.id=xxx
        String paymentId = dataId != null ? dataId : id;
        String notificationType = type != null ? type : topic;

        // Si no hay datos en query params, intentar desde el body JSON
        if ((paymentId == null || paymentId.isBlank()) && body != null && body.getData() != null) {
            paymentId = body.getData().getId();
        }
        if ((notificationType == null || notificationType.isBlank()) && body != null) {
            notificationType = body.getType();
        }

        logger.info("Webhook recibido - Type: {}, PaymentID: {}", notificationType, paymentId);

        if (paymentId == null || paymentId.isBlank() || notificationType == null || notificationType.isBlank()) {
            logger.warn("Webhook recibido sin datos suficientes. Se retorna OK para que MP no reintente.");
            return ResponseEntity.ok("OK");
        }

        try {
            mercadoPagoService.procesarNotificacion(paymentId, notificationType);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            logger.error("Error procesando webhook: {}", e.getMessage(), e);
            // Retornar 200 igual para que MP no reintente indefinidamente
            return ResponseEntity.ok("Procesado con advertencia");
        }
    }

    /**
     * Endpoint alternativo para webhook (Webhooks v2 - body JSON directo)
     * ✅ CORREGIDO: Manejo robusto de excepciones y siempre retorna 200 OK
     */
    @PostMapping("/webhook/notificacion")
    public ResponseEntity<String> recibirNotificacionAlternativa(
            @RequestBody WebhookNotificationDTO notification) {

        logger.info("Webhook alternativo recibido: {}", notification);

        try {
            // ✅ Validar que tiene datos mínimos requeridos
            if (notification != null && notification.getData() != null) {
                String paymentId = notification.getData().getId();
                String type = notification.getType();

                if (paymentId != null && !paymentId.isBlank() && type != null) {
                    logger.debug("Procesando webhook - Type: {}, PaymentID: {}", type, paymentId);
                    mercadoPagoService.procesarNotificacion(paymentId, type);
                } else {
                    logger.warn("Webhook sin datos suficientes - PaymentID: {}, Type: {}", paymentId, type);
                }
            } else {
                logger.warn("Webhook alternativo sin body válido");
            }
        } catch (Exception e) {
            logger.error("Error procesando webhook alternativo: {}", e.getMessage(), e);
            // No relanzar excepción - siempre retornar 200 OK
        }

        // ✅ IMPORTANTE: SIEMPRE retornar 200 OK rápidamente
        // Mercado Pago no reintentar si recibe 200, aunque haya tenido error
        return ResponseEntity.ok("OK");
    }

    /**
     * Endpoint para probar el webhook manualmente
     */
    @PostMapping("/webhook/test")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, String>> testWebhook(
            @RequestParam String paymentId,
            @RequestParam(defaultValue = "payment") String type) {

        logger.info("Test webhook manual - PaymentID: {}, Type: {}", paymentId, type);

        mercadoPagoService.procesarNotificacion(paymentId, type);

        Map<String, String> response = new HashMap<>();
        response.put("status", "procesado");
        response.put("paymentId", paymentId);
        response.put("type", type);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener métodos de pago disponibles
     */
    @GetMapping("/metodos")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'VENDEDOR')")
    public ResponseEntity<Map<String, Object>> getMetodosPago() {

        Map<String, Object> response = new HashMap<>();
        response.put("metodo", "MERCADO_PAGO");
        response.put("nombre", "Mercado Pago");
        response.put("tipos", new String[] { "tarjeta_credito", "tarjeta_debito", "efectivo", "qr" });
        response.put("sandbox", true);
        response.put("tiempo_expiracion", "30 minutos");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de health check para Mercado Pago
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {

        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("modulo", "Mercado Pago");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}
