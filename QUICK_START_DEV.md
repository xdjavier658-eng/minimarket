# 🚀 REFERENCIA RÁPIDA: DESARROLLO LOCAL

## ⚡ COMANDOS ESENCIALES

### Iniciar Backend (Spring Boot)

```bash
cd minimarket_backend

# Primera vez: instalar dependencias
mvn clean install

# Iniciar servidor
mvn spring-boot:run
# ✅ Backend ejecutándose en http://localhost:8080
```

### Iniciar Frontend (Vite/React)

```bash
cd minimarket_frontend

# Primera vez: instalar dependencias
npm install

# Iniciar desarrollo
npm run dev
# ✅ Frontend ejecutándose en http://localhost:3000
```

---

## 📝 EDITAR CREDENCIALES LOCALES

### Backend - Base de datos

```bash
# Editar archivo de configuración
nano minimarket_backend/.env
# o
code minimarket_backend/.env
```

**Reemplazar:**

```env
DATABASE_PASSWORD=postgres  # ← Tu contraseña real
```

### Backend - Mercado Pago

```bash
nano minimarket_backend/.env
```

**Reemplazar:**

```env
MERCADOPAGO_ACCESS_TOKEN=APP_USR-...    # ← Token de Mercado Pago Sandbox
MERCADOPAGO_PUBLIC_KEY=APP_USR-...      # ← Public Key de Mercado Pago Sandbox
```

Obtén los tokens en: https://www.mercadopago.com.pe/developers/panel

### Frontend - API URL

```bash
nano minimarket_frontend/.env
```

**Verificar (debe estar igual):**

```env
VITE_API_URL=http://localhost:8080
VITE_MERCADOPAGO_PUBLIC_KEY=APP_USR-...  # ← Debe coincidir con backend
```

---

## ✅ VERIFICACIÓN DE SALUD

### Backend

```bash
# Verificar que backend está vivo y credenciales son válidas
curl http://localhost:8080/api/payments/health

# Respuesta esperada:
# {"status":"OK","message":"Mercado Pago configured"}
```

### Frontend

Abre en navegador:

```
http://localhost:3000
```

Si no da error de CORS, ¡significa que frontend y backend se conectan! 🎉

---

## 📂 ESTRUCTURA DE ARCHIVOS IMPORTANTES

```
DESARROLLO LOCAL:
├── minimarket_backend/
│   ├── .env                 ← EDITAR CON TUS CREDENCIALES
│   ├── .env.example         ← Plantilla de referencia (no editar)
│   └── src/main/resources/
│       └── application.yml  ← Lee desde .env automáticamente
│
└── minimarket_frontend/
    ├── .env                 ← EDITAR CON TU API_URL
    ├── .env.example         ← Plantilla de referencia (no editar)
    └── src/config/
        └── env.js           ← Importa desde .env automáticamente
```

---

## 🚨 VARIABLES CRÍTICAS (.env BACKEND)

| Variable                   | Requerida | Tipo   | Ejemplo         |
| -------------------------- | --------- | ------ | --------------- |
| `DATABASE_PASSWORD`        | ✅ SÍ     | string | `postgres`      |
| `MERCADOPAGO_ACCESS_TOKEN` | ✅ SÍ     | string | `APP_USR-...`   |
| `MERCADOPAGO_PUBLIC_KEY`   | ✅ SÍ     | string | `APP_USR-...`   |
| `JWT_SECRET`               | ✅ SÍ     | string | Mínimo 32 chars |

---

## 🌍 VARIABLES CRÍTICAS (.env FRONTEND)

| Variable                      | Requerida | Tipo   | Ejemplo                 |
| ----------------------------- | --------- | ------ | ----------------------- |
| `VITE_API_URL`                | ✅ SÍ     | string | `http://localhost:8080` |
| `VITE_MERCADOPAGO_PUBLIC_KEY` | ✅ SÍ     | string | `APP_USR-...`           |

---

## 🔍 VERIFICAR QUE TODO FUNCIONA

### Test 1: Backend lee variables

```bash
# Ver logs cuando inicia Spring Boot
mvn spring-boot:run 2>&1 | grep -i "mercado\|database\|jwt"
# Debe mostrar: "MercadoPagoCredentialsConfig loaded" o similar
```

### Test 2: Frontend lee variables

```bash
# En consola del navegador (F12)
import.meta.env.VITE_API_URL
# Debe mostrar: "http://localhost:8080"
```

### Test 3: Conexión Backend-Frontend

```bash
# En frontend, abrir DevTools (F12)
# Ir a Network tab
# Hacer login
# Debe ver request a: http://localhost:8080/api/auth/signin
# Status: 200 o 401 (dependiendo credenciales)
```

---

## 💡 TIPS ÚTILES

### Regenerar JWT_SECRET seguro

```bash
# Windows
openssl rand -base64 32

# Mac/Linux
openssl rand -base64 32

# Resultado: copia este valor a minimarket_backend/.env
```

### Ver contenido de .env sin editarlo

```bash
# Backend
cat minimarket_backend/.env

# Frontend
cat minimarket_frontend/.env
```

### Reiniciar después de cambiar .env

```bash
# Backend
Ctrl+C  (en terminal donde corre mvn)
mvn spring-boot:run  (iniciar de nuevo)

# Frontend
Ctrl+C  (en terminal donde corre npm run dev)
npm run dev  (iniciar de nuevo)
```

---

## 🆘 ERRORES COMUNES

### Error: "Could not resolve dependency"

**Causa:** Maven no descargó `spring-dotenv`

**Solución:**

```bash
cd minimarket_backend
mvn clean install
```

### Error: "CORS policy"

**Causa:** Frontend y backend no ven el mismo `VITE_API_URL`

**Solución:**

```bash
# Verifica que .env en frontend tenga:
VITE_API_URL=http://localhost:8080
# Y backend corre en puerto 8080
```

### Error: "Mercado Pago credentials not configured"

**Causa:** `MERCADOPAGO_ACCESS_TOKEN` está vacío

**Solución:**

```bash
# Editar minimarket_backend/.env
# Agregar token real de MP
MERCADOPAGO_ACCESS_TOKEN=APP_USR-xxxx...
# Reiniciar backend
```

---

## 📊 ESTADOS ESPERADOS

### ✅ TODO FUNCIONA

```
✅ Backend http://localhost:8080 corriendo
✅ Frontend http://localhost:3000 corriendo
✅ API requests sin CORS errors
✅ Login funciona con credenciales válidas
✅ Mercado Pago modal abre sin errores
```

### ⚠️ PROBLEMA

```
❌ Backend no inicia → Revisar .env
❌ Frontend no inicia → Revisar npm packages
❌ CORS error → Revisar VITE_API_URL
❌ API 401 → Credenciales incorrectas
```

---

¡Ahora estás listo para desarrollar! 🚀
