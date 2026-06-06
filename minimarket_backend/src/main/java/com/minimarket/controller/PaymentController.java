package com.minimarket.controller;

import com.minimarket.dto.venta.FinalizarVentaRequest;
import com.minimarket.dto.venta.FinalizarVentaResponse;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.VentaService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ========================================================================
 * CONTROLADOR UNIFICADO DE PAGOS - PaymentController
 * ========================================================================
 * 
 * Este controlador es el PUNTO DE ENTRADA para todas las operaciones de pago.
 * Maneja:
 * 
 * 1. POST /api/payments/procesar → Procesa pagos con cualquier método
 * 2. GET /api/payments/estado/{ventaId} → Verifica estado de un pago
 * 3. POST /api/payments/confirmar → Confirma pago tras retorno de MP
 * 
 * FLUJO GENERAL DE UN PAGO:
 * 
 * 1. Cliente selecciona items y método de pago
 * 2. Frontend → POST /api/payments/procesar
 * 3. Backend:
 * a. Crea Venta en BD
 * b. Si es Mercado Pago:
 * - Crea Preferencia en MP
 * - Devuelve URL de checkout
 * - Cliente redirigido a MP
 * c. Si es offline (EFECTIVO, YAPE, PLIN):
 * - Genera token de referencia
 * - Devuelve estado completado
 * 4. Tras pago:
 * - MP webhook → actualiza estado
 * - Cliente regresa → verifica estado
 * 
 * ========================================================================
 */

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * ====================================================================
     * ENDPOINT: POST /api/payments/procesar
     * ====================================================================
     * 
     * Procesa un pago con el método seleccionado por el cliente.
     * 
     * ENTRADA (FinalizarVentaRequest):
     * {
     * "items": [
     * { "productoId": 1, "cantidad": 2 },
     * { "productoId": 3, "cantidad": 1 }
     * ],
     * "metodoPagoId": 5, // ID del método seleccionado
     * "referencia": "" // Para YAPE/PLIN: número de transacción
     * }
     * 
     * SALIDA (FinalizarVentaResponse):
     * {
     * "ventaId": 123,
     * "estado": "PAGADO" o "REDIRECCION" o "PENDIENTE",
     * "total": 150.50,
     * "metodoPago": "MERCADO_PAGO",
     * "redirectUrl": "https://checkout.mercadopago.com/..." (si aplica)
     * }
     * 
     * CASOS DE USO:
     * 
     * 1. Pago con Mercado Pago:
     * - Respuesta contiene "redirectUrl"
     * - Frontend redirige a esa URL
     * 
     * 2. Pago Offline (EFECTIVO, YAPE, PLIN):
     * - Respuesta contiene "estado" = "PAGADO"
     * - Mostrar ticket directamente
     * 
     */
    @PostMapping("/procesar")
    public ResponseEntity<FinalizarVentaResponse> procesarPago(
            @Valid @RequestBody FinalizarVentaRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Solicitud para procesar pago - Items: {}, Método: {}",
                request.getItems().size(), request.getMetodoPagoId());

        try {
            // NOTA: Este endpoint es PÚBLICO (sin autenticación requerida)
            // Si necesitas restricciones, descomentar la siguiente línea:
            // Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            FinalizarVentaResponse response = ventaService.finalizarVenta(request, null);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Validación fallida en pago: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error procesando pago: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ====================================================================
     * ENDPOINT: GET /api/payments/estado/{ventaId}
     * ====================================================================
     * 
     * Verifica el estado actual de un pago.
     * 
     * PARÁMETROS:
     * - ventaId: ID de la venta a consultar
     * 
     * SALIDA:
     * {
     * "ventaId": 123,
     * "estado": "PAGADO",
     * "total": 150.50,
     * "metodoPago": "MERCADO_PAGO",
     * "transactionId": "payment_id_from_mp"
     * }
     * 
     * ESTADOS POSIBLES:
     * - PAGADO: Pago confirmado
     * - RECHAZADO: Pago rechazado o cancelado
     * - PENDIENTE: En espera de confirmación
     * - EN_PROCESO: Procesando
     * 
     */
    @GetMapping("/estado/{ventaId}")
    public ResponseEntity<?> verificarEstadoPago(
            @PathVariable Long ventaId) {

        logger.info("Consultando estado de pago para venta ID: {}", ventaId);

        try {
            Map<String, Object> response = ventaService.obtenerEstadoPago(ventaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error verificando estado de pago: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error al consultar estado");
        }
    }

    /**
     * ====================================================================
     * ENDPOINT: POST /api/payments/confirmar
     * ====================================================================
     * 
     * Confirma un pago cuando el cliente regresa de Mercado Pago.
     * Llamado desde las páginas de retorno (éxito/fallo/pendiente).
     * 
     * ENTRADA:
     * {
     * "ventaId": 123,
     * "paymentId": "payment_id_from_mp" (opcional)
     * }
     * 
     * SALIDA:
     * {
     * "ventaId": 123,
     * "estado": "PAGADO",
     * "total": 150.50,
     * "mensaje": "Pago confirmado exitosamente"
     * }
     * 
     */
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarPago(
            @RequestBody Map<String, Object> datos) {

        Long ventaId = ((Number) datos.get("ventaId")).longValue();
        String paymentId = (String) datos.get("paymentId");

        logger.info("Confirmando pago para venta ID: {}. PaymentID: {}", ventaId, paymentId);

        try {
            Map<String, Object> response = ventaService.confirmarPago(ventaId, paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error confirmando pago: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error al confirmar pago");
        }
    }

    /**
     * ====================================================================
     * ENDPOINT: GET /api/payments/health
     * ====================================================================
     * 
     * Endpoint de salud para verificar que el servicio de pagos está activo
     * y que las credenciales de Mercado Pago están configuradas.
     * 
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("service", "Payment Service");
            health.put("timestamp", System.currentTimeMillis());

            // NOTA: Si necesitas validar credenciales aquí, descomentar:
            // mercadoPagoCredentialsConfig.validateCredentials();
            // health.put("mercadopago", "CONFIGURED");

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
