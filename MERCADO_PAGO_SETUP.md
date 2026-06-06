# 🚀 GUÍA: INTEGRACIÓN DE MERCADO PAGO - MINIMARKET

> Instrucciones paso a paso para completar la integración de Mercado Pago en tu proyecto.

---

## 📋 TABLA DE CONTENIDOS

1. [Obtener Credenciales](#obtener-credenciales)
2. [Configurar Backend](#configurar-backend)
3. [Probar en Sandbox](#probar-en-sandbox)
4. [Frontend - Modal de Pago](#frontend---modal-de-pago)
5. [Webhook & Notificaciones](#webhook--notificaciones)
6. [Troubleshooting](#troubleshooting)

---

## 1️⃣ OBTENER CREDENCIALES

### Paso 1.1: Crear cuenta en Mercado Pago

1. Ve a: https://www.mercadopago.com.pe
2. Haz clic en **"Vender"** → **"Crear Cuenta"**
3. Completa el formulario con tus datos
4. Verifica tu email

### Paso 1.2: Acceder al Panel de Desarrolladores

1. Inicia sesión: https://www.mercadopago.com.pe
2. Haz clic en tu **foto de perfil** (arriba a la derecha)
3. Selecciona: **"Configuración"** → **"Credenciales"**
   - O accede directamente: https://www.mercadopago.com.pe/developers/panel

### Paso 1.3: Copiar Credenciales de Sandbox (Prueba)

Verás dos columnas: **PRODUCCIÓN** y **PRUEBA**

⚠️ **PARA DESARROLLO, USA LA COLUMNA "PRUEBA" (Sandbox)**

Copia estos valores:

| Campo            | Valor                  | Ubicación      |
| ---------------- | ---------------------- | -------------- |
| **Access Token** | `APP_USR-XXXX-XXXX...` | Columna PRUEBA |
| **Public Key**   | `APP_USR-XXXX-XXXX...` | Columna PRUEBA |

✅ Guarda ambos en un archivo de texto temporal (los necesitarás en los próximos pasos)

---

## 2️⃣ CONFIGURAR BACKEND

### Paso 2.1: Editar `application.yml`

1. Abre el archivo: `minimarket_backend/src/main/resources/application.yml`

2. Busca la sección `mercadopago:` (o créala si no existe)

3. Reemplaza con TUS CREDENCIALES:

```yaml
mercadopago:
  # ⬇️ REEMPLAZA ESTOS VALORES CON LOS TUYOS
  access-token: "APP_USR-XXXXXXXXXXXXXXXX"
  public-key: "APP_USR-XXXXXXXXXXXXXXXX"

  sandbox: true # ✅ DÉJALO EN true PARA DESARROLLO

  success-url: "http://localhost:3000/pago-exitoso"
  failure-url: "http://localhost:3000/pago-fallido"
  pending-url: "http://localhost:3000/pago-pendiente"
  notification-url: "http://localhost:8080/api/mercadopago/webhook"
  expiration-minutes: 30
```

### Paso 2.2: Verificar Dependencies

Abre `minimarket_backend/pom.xml` y asegúrate que estas dependencias existan:

```xml
<!-- Mercado Pago SDK -->
<dependency>
    <groupId>com.mercadopago</groupId>
    <artifactId>sdk-java</artifactId>
    <version>2.1.16</version>
</dependency>

<!-- QRCode para pago con YAPE/PLIN -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.0</version>
</dependency>
```

Si faltan, agrégalas manualmente.

### Paso 2.3: Actualizar URLs de Retorno en Mercado Pago

1. Ve a: https://www.mercadopago.com.pe/developers/panel/settings/integration
2. Busca: **"URLs de Retorno"**
3. Configura:
   - **Success URL**: `http://localhost:3000/pago-exitoso`
   - **Failure URL**: `http://localhost:3000/pago-fallido`
   - **Pending URL**: `http://localhost:3000/pago-pendiente`
4. Guarda los cambios

---

## 3️⃣ PROBAR EN SANDBOX

### Paso 3.1: Iniciar Backend

```bash
cd minimarket_backend

# Limpiar y construir
mvn clean install

# Ejecutar (en terminal)
mvn spring-boot:run
```

Deberías ver en logs algo como:

```
INFO: MercadoPagoService initialized
INFO: Server started on http://localhost:8080
```

### Paso 3.2: Iniciar Frontend

```bash
cd minimarket_frontend

npm install
npm run dev
```

Frontend debe estar en: `http://localhost:3000`

### Paso 3.3: Probar Pago

1. Abre: http://localhost:3000
2. Añade productos al carrito
3. Haz clic en **"Finalizar Compra"**
4. Selecciona **"Mercado Pago"** en el modal
5. Haz clic en **"Confirmar Pago"**
6. Deberías ser redirigido a Mercado Pago Sandbox

### Paso 3.4: Datos de Prueba en Sandbox

Para probar pagos, Mercado Pago proporciona tarjetas de prueba:

**TARJETA VÁLIDA (Aprobado):**

- Número: `4111 1111 1111 1111`
- Expiración: Cualquier fecha futura
- CVV: Cualquier número (3 dígitos)
- Titular: Cualquier nombre

**TARJETA RECHAZADA:**

- Número: `5555 5555 5555 4444`
- (El resultado será "rechazado")

Más tarjetas de prueba: https://www.mercadopago.com.pe/developers/es/guides/resources/localization/sdk

---

## 4️⃣ FRONTEND - MODAL DE PAGO

### Características del Nuevo Modal

El modal que se activó tiene:

✅ **Diseño moderno** con colores:

- Fondo azul (#003366)
- Acentos verde lima (#CDDC39)
- Amarillo (#FFD700)

✅ **Layout en dos columnas:**

- Izquierda: Métodos de pago (tarjetas clicables)
- Derecha: Resumen de carrito + total

✅ **Métodos de pago soportados:**

- 💳 Mercado Pago (tarjeta/QR)
- 💵 Efectivo
- 📱 Yape
- 📲 Plin

### Ubicación del Código

| Archivo    | Ubicación                                                |
| ---------- | -------------------------------------------------------- |
| Componente | `minimarket_frontend/src/components/MetodoPagoModal.jsx` |
| Estilos    | `minimarket_frontend/src/components/MetodoPagoModal.css` |
| Hook       | `minimarket_frontend/src/hooks/useCart.jsx`              |
| Contexto   | `minimarket_frontend/src/context/CartContext.jsx`        |

### Personalización del Modal

Para cambiar colores, edita `MetodoPagoModal.css`:

```css
:root {
  --primary-blue: #003366; /* Azul principal */
  --accent-lime: #cddc39; /* Verde lima */
  --accent-yellow: #ffd700; /* Amarillo */
  --success-green: #4caf50; /* Verde de éxito */
}
```

---

## 5️⃣ WEBHOOK & NOTIFICACIONES

### Qué es un Webhook

Cuando un usuario paga en Mercado Pago, MP **envía una notificación** (IPN - Instant Payment Notification) a tu backend para confirmar el estado.

### Configurar Webhook para Desarrollo Local

**Problema:** `localhost:8080` no es accesible desde internet (MP no puede llegar)

**Solución:** Usar **ngrok** (túnel HTTPS a tu localhost)

#### Paso 5.1: Descargar ngrok

1. Ve a: https://ngrok.com/download
2. Descarga para tu SO (Windows/Mac/Linux)
3. Descomprime en una carpeta (ej: `C:\ngrok`)

#### Paso 5.2: Ejecutar ngrok

```bash
# Navega a la carpeta de ngrok
cd C:\ngrok  # Windows
# o
cd ~/ngrok   # Mac/Linux

# Expone el puerto 8080
./ngrok http 8080
```

Verás algo como:

```
ngrok by @inconshreveable

Session Status                online
Session expires               7 hours, 59 minutes
Version                       3.0.0
Region                        us (United States)
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://1234-56-78-90-123.ngrok.io -> http://localhost:8080
```

Copia la URL: `https://1234-56-78-90-123.ngrok.io`

#### Paso 5.3: Actualizar `application.yml`

```yaml
mercadopago:
  notification-url: "https://1234-56-78-90-123.ngrok.io/api/mercadopago/webhook"
```

#### Paso 5.4: Verificar Webhook en MP

1. Ve a: https://www.mercadopago.com.pe/developers/panel/webhooks
2. Configura la URL de webhook
3. MP probará la conexión (deberías ver un estado "OK")

---

## 6️⃣ TROUBLESHOOTING

### ❌ "Access Token no configurado"

**Error:** `IllegalStateException: Credenciales de Mercado Pago no configuradas`

**Solución:**

1. Verifica que `application.yml` tenga la sección `mercadopago:`
2. Reemplaza `YOUR_ACCESS_TOKEN_HERE` con tu token real
3. Reinicia el backend: `mvn spring-boot:run`

---

### ❌ "Invalid Access Token"

**Error:** `401 Unauthorized` desde la API de MP

**Solución:**

1. Verifica que copiaste el token **CORRECTO** desde panel (no confundas Public Key con Access Token)
2. Asegúrate de usar token de **SANDBOX** (comienza con `APP_USR-`)
3. El token expira si pasó mucho tiempo: copia uno nuevo del panel

---

### ❌ "Webhook no recibe notificaciones"

**Error:** La venta está creada pero no se actualiza a "PAGADO"

**Solución:**

1. Verifica ngrok está ejecutándose: `ngrok http 8080`
2. Actualiza `notification-url` en `application.yml` con la URL de ngrok
3. Reinicia el backend
4. Prueba el webhook en el panel de MP (busca botón "Enviar prueba")

---

### ❌ "Modal no abre / error de estilos"

**Error:** El modal no aparece o se ve roto

**Solución:**

1. Verifica que `MetodoPagoModal.css` está importado en `MetodoPagoModal.jsx`
2. Abre DevTools (F12) → Revisa console por errores de CSS
3. Limpia caché: `npm run build` y recarga la página

---

### ❌ "Carrito no se limpia después de pagar"

**Error:** Tras pagar, los productos siguen en el carrito

**Solución:**

1. Verifica que `finalizarVentaPublico()` en `api.js` devuelve estado "REDIRECCION" o "PAGADO"
2. El método `limpiarCarrito()` debe ser llamado después
3. Revisa `MetodoPagoModal.jsx` línea ~75-85

---

## 📞 SOPORTE

Si algo no funciona:

1. **Revisa los logs del backend:** Terminal donde ejecutaste `mvn spring-boot:run`
2. **Revisa la consola del navegador:** F12 → Console
3. **Verifica credenciales:** ¿Están correctas en `application.yml`?
4. **Prueba endpoint de salud:** GET http://localhost:8080/api/payments/health

---

## ✅ CHECKLIST FINAL

Antes de lanzar a producción:

- [ ] Credenciales de Sandbox configuradas en `application.yml`
- [ ] Modal de pago funciona y abre en checkout
- [ ] Pago de prueba completado exitosamente
- [ ] Webhook recibe notificaciones (verificar en logs)
- [ ] Venta se marca como "PAGADO" en BD
- [ ] Stock se descuenta correctamente
- [ ] Ticket se genera correctamente
- [ ] Pagas con tarjeta de prueba: **sin problemas**
- [ ] Pagas con tarjeta rechazada: **error manejado correctamente**

Una vez todo funcione:

1. Solicita credenciales de **PRODUCCIÓN** a Mercado Pago
2. Reemplaza en `application.yml`: `sandbox: false`
3. Actualiza `access-token` y `public-key` con credenciales de PRODUCCIÓN
4. Implementa verificación SSL/HTTPS
5. Prueba con dinero real (recomendado: transacción pequeña primero)

---

¡Listo! Tu integración de Mercado Pago está completa. 🎉

Para más información: https://www.mercadopago.com.pe/developers/
