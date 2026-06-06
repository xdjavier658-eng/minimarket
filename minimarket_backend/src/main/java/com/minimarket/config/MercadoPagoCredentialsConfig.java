package com.minimarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * ========================================================================
 * CONFIGURACIÓN DE CREDENCIALES - MERCADO PAGO
 * ========================================================================
 * 
 * INSTRUCCIONES PARA IMPLEMENTAR:
 * 
 * 1. Copia tus credenciales desde:
 * https://www.mercadopago.com.pe/developers/panel
 * 
 * 2. Abre el archivo: application.properties o application.yml
 * (ubicado en: src/main/resources/application.properties)
 * 
 * 3. Añade/actualiza las siguientes líneas CON TUS CREDENCIALES:
 * 
 * =====================================================================
 * PARA application.properties:
 * =====================================================================
 * 
 * # Mercado Pago Configuration
 * mercadopago.access-token=YOUR_ACCESS_TOKEN_HERE
 * mercadopago.public-key=YOUR_PUBLIC_KEY_HERE
 * mercadopago.sandbox=true
 * mercadopago.success-url=http://localhost:3000/pago-exitoso
 * mercadopago.failure-url=http://localhost:3000/pago-fallido
 * mercadopago.pending-url=http://localhost:3000/pago-pendiente
 * mercadopago.notification-url=http://localhost:8080/api/mercadopago/webhook
 * mercadopago.expiration-minutes=30
 * 
 * =====================================================================
 * O PARA application.yml:
 * =====================================================================
 * 
 * mercadopago:
 * access-token: YOUR_ACCESS_TOKEN_HERE
 * public-key: YOUR_PUBLIC_KEY_HERE
 * sandbox: true
 * success-url: http://localhost:3000/pago-exitoso
 * failure-url: http://localhost:3000/pago-fallido
 * pending-url: http://localhost:3000/pago-pendiente
 * notification-url: http://localhost:8080/api/mercadopago/webhook
 * expiration-minutes: 30
 * 
 * =====================================================================
 * 
 * 4. OBTENER TUS CREDENCIALES:
 * 
 * a) Accede a: https://www.mercadopago.com.pe/developers/panel
 * b) Inicia sesión con tu cuenta de Mercado Pago
 * c) En el panel izquierdo, selecciona "Credenciales"
 * d) Encontrarás dos conjuntos:
 * - PRODUCCIÓN (Live Credentials)
 * - PRUEBA (Sandbox Credentials) ← Usa estas para desarrollo
 * e) Copia el "Access Token" (comienza con "APP_USR-...")
 * f) Copia el "Public Key" (comienza con "APP_USR-...")
 * 
 * 5. REEMPLAZA ESTOS VALORES:
 * - YOUR_ACCESS_TOKEN_HERE → Tu Access Token
 * - YOUR_PUBLIC_KEY_HERE → Tu Public Key
 * 
 * 6. CONFIGURACIÓN DE URLs PARA DESARROLLO:
 * - Si trabajas en localhost, deja las URLs como están
 * - Si usas un servidor remoto, actualiza "localhost" con tu IP/dominio
 * - La URL de webhook debe ser accesible desde internet
 * (considera usar ngrok para testing: ngrok http 8080)
 * 
 * ========================================================================
 * 
 * NOTAS IMPORTANTES:
 * 
 * - NUNCA subas credenciales reales a Git. Usa variables de entorno.
 * - En producción, usa las credenciales LIVE, no SANDBOX.
 * - El webhook URL debe ser accesible desde Mercado Pago.
 * - Para testing local, usa ngrok: https://ngrok.com/
 * 
 * ========================================================================
 */

@Configuration
@Component
public class MercadoPagoCredentialsConfig {

    @Value("${mercadopago.access-token:NOT_SET}")
    private String accessToken;

    @Value("${mercadopago.public-key:NOT_SET}")
    private String publicKey;

    @Value("${mercadopago.sandbox:true}")
    private Boolean isSandbox;

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

    /**
     * Validar que las credenciales estén configuradas
     */
    public void validateCredentials() {
        if ("NOT_SET".equals(accessToken) || accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "Credenciales de Mercado Pago no configuradas. " +
                            "Por favor, añade 'mercadopago.access-token' en application.properties o application.yml");
        }
        if ("NOT_SET".equals(publicKey) || publicKey == null || publicKey.isBlank()) {
            throw new IllegalStateException(
                    "Clave pública de Mercado Pago no configurada. " +
                            "Por favor, añade 'mercadopago.public-key' en application.properties o application.yml");
        }
    }

    // Getters
    public String getAccessToken() {
        return accessToken;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Boolean getIsSandbox() {
        return isSandbox;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public String getPendingUrl() {
        return pendingUrl;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public Integer getExpirationMinutes() {
        return expirationMinutes;
    }
}
