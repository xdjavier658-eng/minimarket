// src/config/env.js
//
// Configuración centralizada de variables de entorno
// Las variables VITE_* se definen en .env y se inyectan en tiempo de build
//
// ✅ USO:
//    import { API_URL, MERCADOPAGO_PUBLIC_KEY } from '@/config/env'
//    console.log(API_URL)  // → "http://localhost:8080/api"

export const API_URL = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api`
  : "/api";

export const API_TIMEOUT = parseInt(
  import.meta.env.VITE_API_TIMEOUT || "30000",
);

export const MERCADOPAGO_PUBLIC_KEY = import.meta.env
  .VITE_MERCADOPAGO_PUBLIC_KEY;

export const MERCADOPAGO_SANDBOX =
  import.meta.env.VITE_MERCADOPAGO_SANDBOX === "true";

export const APP_NAME = import.meta.env.VITE_APP_NAME || "Minimarket";

export const APP_VERSION = import.meta.env.VITE_APP_VERSION || "1.0.0";

export const ENVIRONMENT = import.meta.env.VITE_ENVIRONMENT || "development";

export const ENABLE_ANALYTICS =
  import.meta.env.VITE_ENABLE_ANALYTICS === "true";

// ============================================================================
// ⚠️  VALIDACIÓN DE CREDENCIALES CRÍTICAS
// ============================================================================

export function validateEnvironment() {
  const errors = [];

  if (!MERCADOPAGO_PUBLIC_KEY) {
    errors.push("❌ VITE_MERCADOPAGO_PUBLIC_KEY no configurada en .env");
  }

  if (errors.length > 0) {
    console.error("🚨 ERRORES DE CONFIGURACIÓN:");
    errors.forEach((err) => console.error(err));
    console.warn(
      "📋 Asegúrate de que .env existe y tiene todas las variables requeridas",
    );
    return false;
  }

  console.log("✅ Ambiente configurado correctamente");
  return true;
}

// Log para desarrollo
if (ENVIRONMENT === "development") {
  console.log("📌 CONFIGURACIÓN DE AMBIENTE:", {
    API_URL,
    API_TIMEOUT,
    MERCADOPAGO_SANDBOX,
    ENVIRONMENT,
  });
}
