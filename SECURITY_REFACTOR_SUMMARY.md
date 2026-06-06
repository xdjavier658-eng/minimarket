# 🔐 REFACTORIZACIÓN DE SEGURIDAD: VARIABLES DE ENTORNO

## 📋 RESUMEN EJECUTIVO

Tu proyecto **Minimarket** ha sido completamente refactorizado para proteger todas las credenciales usando variables de entorno.

**Lo que cambió:**

- ✅ **Antes:** Credenciales hardcodeadas en `application.yml` y código
- ✅ **Ahora:** 100% de credenciales en archivos `.env` (no versionados en Git)

**Seguridad alcanzada:**

- 🔒 Database password: **Protegida**
- 🔒 JWT Secret: **Protegida**
- 🔒 Mercado Pago Tokens: **Protegidas**
- 🔒 API URLs: **Configurables por ambiente**

---

## 🚀 GUÍA RÁPIDA DE INICIO

### Para el Backend (Spring Boot)

1. **Ya existe** un archivo `.env` en `minimarket_backend/.env` con valores dummy
2. **Edítalo** con tus credenciales reales:
   ```bash
   cd minimarket_backend
   # Editar .env y reemplazar:
   # - DATABASE_PASSWORD=tu_contraseña_postgres_real
   # - MERCADOPAGO_ACCESS_TOKEN=APP_USR-...
   # - JWT_SECRET=clave_segura_de_64_caracteres_minimo
   ```
3. **Reinicia** el backend:
   ```bash
   mvn spring-boot:run
   ```
   Spring Boot automáticamente leerá `.env` gracias a `spring-dotenv`

### Para el Frontend (Vite/React)

1. **Ya existe** un archivo `.env` en `minimarket_frontend/.env` con valores dummy
2. **Edítalo** con tus URLs:
   ```bash
   cd minimarket_frontend
   # Editar .env y reemplazar:
   # - VITE_API_URL=http://localhost:8080 (o tu URL de producción)
   # - VITE_MERCADOPAGO_PUBLIC_KEY=APP_USR-...
   ```
3. **Reinicia** el frontend:
   ```bash
   npm run dev
   ```
   Vite automáticamente leerá `.env`

---

## 📂 ESTRUCTURA DE ARCHIVOS

```
minimarket/
├── .env.example                              ← Plantilla GLOBAL (sube a Git)
├── .env                                      ← Variables locales (NO sube a Git)
├── .gitignore                                ← Bloquea .env pero permite .env.example
│
├── minimarket_backend/
│   ├── .env.example                          ← Plantilla BACKEND (sube a Git)
│   ├── .env                                  ← Variables privadas BACKEND (no sube)
│   ├── .gitignore                            ← Bloquea .env pero permite .env.example
│   ├── pom.xml                               ← Agregada dependencia spring-dotenv
│   └── src/main/resources/
│       └── application.yml                   ← 100% variables de entorno
│
└── minimarket_frontend/
    ├── .env.example                          ← Plantilla FRONTEND (sube a Git)
    ├── .env                                  ← Variables privadas FRONTEND (no sube)
    ├── .gitignore                            ← Bloquea .env pero permite .env.example
    └── src/
        ├── api/
        │   └── api.js                        ← Actualizado: usa VITE_API_URL
        └── config/
            └── env.js                        ← NUEVO: configuración centralizada
```

---

## 🔍 QUÉ SE MODIFICÓ

### Backend (Spring Boot)

| Archivo           | Cambio                  | Detalles                                   |
| ----------------- | ----------------------- | ------------------------------------------ |
| `pom.xml`         | ✅ Agregada dependencia | `me.paulschwarz:spring-dotenv:4.0.0`       |
| `application.yml` | ✅ 100% variables       | Database, JWT, Mercado Pago sin hardcoding |
| `.env.example`    | ✅ Creado               | Plantilla pública con todas las variables  |
| `.env`            | ✅ Creado               | Archivo privado con valores dummy (editar) |
| `.gitignore`      | ✅ Actualizado          | Bloquea `.env`, permite `.env.example`     |

### Frontend (Vite/React)

| Archivo             | Cambio          | Detalles                                   |
| ------------------- | --------------- | ------------------------------------------ |
| `src/config/env.js` | ✅ Creado NUEVO | Configuración centralizada de variables    |
| `src/api/api.js`    | ✅ Actualizado  | Lee `VITE_API_URL` desde `.env`            |
| `.env.example`      | ✅ Creado       | Plantilla pública con todas las variables  |
| `.env`              | ✅ Creado       | Archivo privado con valores dummy (editar) |
| `.gitignore`        | ✅ Actualizado  | Bloquea `.env`, permite `.env.example`     |

### Raíz del Proyecto

| Archivo      | Cambio         | Detalles                                             |
| ------------ | -------------- | ---------------------------------------------------- |
| `.gitignore` | ✅ Actualizado | Regla global: bloquea `.env`, permite `.env.example` |

---

## 🔐 VARIABLES DE ENTORNO CONFIGURADAS

### Backend (application.yml)

```yaml
# Base de datos
DATABASE_URL=${DATABASE_URL:jdbc:postgresql://localhost:5432/minimarket_db}
DATABASE_USER=${DATABASE_USER:postgres}
DATABASE_PASSWORD=${DATABASE_PASSWORD}  # ← SIN valor por defecto (REQUERIDO)

# JWT
JWT_SECRET=${JWT_SECRET:CHANGE_ME_...}
JWT_EXPIRATION=${JWT_EXPIRATION:86400000}

# Mercado Pago
MERCADOPAGO_ACCESS_TOKEN=${MERCADOPAGO_ACCESS_TOKEN}  # ← REQUERIDO
MERCADOPAGO_PUBLIC_KEY=${MERCADOPAGO_PUBLIC_KEY}      # ← REQUERIDO
MERCADOPAGO_SUCCESS_URL=${MERCADOPAGO_SUCCESS_URL:http://localhost:3000/pago-exitoso}
MERCADOPAGO_FAILURE_URL=${MERCADOPAGO_FAILURE_URL:http://localhost:3000/pago-fallido}
MERCADOPAGO_PENDING_URL=${MERCADOPAGO_PENDING_URL:http://localhost:3000/pago-pendiente}
MERCADOPAGO_NOTIFICATION_URL=${MERCADOPAGO_NOTIFICATION_URL:http://localhost:8080/...}
MERCADOPAGO_EXPIRATION_MINUTES=${MERCADOPAGO_EXPIRATION_MINUTES:30}
MERCADOPAGO_SANDBOX=${MERCADOPAGO_SANDBOX:true}
```

### Frontend (.env)

```bash
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
VITE_MERCADOPAGO_PUBLIC_KEY=APP_USR-...
VITE_MERCADOPAGO_SANDBOX=true
VITE_APP_NAME=Minimarket
VITE_APP_VERSION=1.0.0
VITE_ENVIRONMENT=development
VITE_ENABLE_ANALYTICS=false
```

---

## ✅ CÓMO USAR EN COMPONENTES

### Opción A: Usar configuración centralizada (RECOMENDADO)

```javascript
// En cualquier archivo .js o .jsx
import { API_URL, MERCADOPAGO_PUBLIC_KEY, ENVIRONMENT } from '@/config/env'

// Usar directamente
fetch(`${API_URL}/auth/signin`, { ... })

console.log(`Ambiente: ${ENVIRONMENT}`)  // "development", "staging", "production"
```

### Opción B: Acceso directo a variables

```javascript
// Si prefieres acceso directo (NO recomendado)
const apiUrl = import.meta.env.VITE_API_URL;
const publicKey = import.meta.env.VITE_MERCADOPAGO_PUBLIC_KEY;
```

---

## 🌍 PARA PRODUCCIÓN

### Backend

1. **Crear `.env` de producción:**

   ```bash
   # minimarket_backend/.env (en servidor de producción)
   DATABASE_URL=jdbc:postgresql://prod-server:5432/minimarket_db
   DATABASE_USER=prod_user
   DATABASE_PASSWORD=super_contraseña_segura_aqui

   JWT_SECRET=clave_jwt_super_larga_de_64_caracteres_minimo_aleatorio

   MERCADOPAGO_ACCESS_TOKEN=APP_USR-PROD-...
   MERCADOPAGO_PUBLIC_KEY=APP_USR-PROD-...
   MERCADOPAGO_SANDBOX=false
   ```

2. **Desplegar:**
   ```bash
   git clone https://github.com/xdjavier658-eng/minimarket.git
   cd minimarket/minimarket_backend
   # ⚠️  Crear .env con credenciales de producción (NO del repositorio)
   mvn clean install
   mvn spring-boot:run
   ```

### Frontend

1. **Crear `.env.production`:**

   ```bash
   # minimarket_frontend/.env.production
   VITE_API_URL=https://api.tu-dominio.com
   VITE_MERCADOPAGO_PUBLIC_KEY=APP_USR-PROD-...
   VITE_MERCADOPAGO_SANDBOX=false
   VITE_ENVIRONMENT=production
   ```

2. **Build y desplegar:**
   ```bash
   npm run build  # Vite leerá .env.production automáticamente
   # Subir contenido de dist/ a tu servidor web
   ```

---

## 🚨 SEGURIDAD: CHECKLIST

- [ ] **Backend:**
  - [ ] `.env` creado con credenciales REALES (no dummy)
  - [ ] `DATABASE_PASSWORD` no vacío
  - [ ] `JWT_SECRET` es una clave de 64+ caracteres
  - [ ] `MERCADOPAGO_ACCESS_TOKEN` y `PUBLIC_KEY` válidos
  - [ ] `.env` NO está en git (verificar con `git status`)

- [ ] **Frontend:**
  - [ ] `.env` creado con `VITE_API_URL` correcto
  - [ ] `VITE_MERCADOPAGO_PUBLIC_KEY` válido
  - [ ] `.env` NO está en git (verificar con `git status`)

- [ ] **GitHub:**
  - [ ] `.env.example` SÍ está en git (verifica `git log --oneline | grep "FASE"`)
  - [ ] `.env` NO está en git
  - [ ] `.gitignore` bloquea `.env` en ambos proyectos

---

## 🔧 TROUBLESHOOTING

### Backend no inicia: "Credenciales de Mercado Pago no configuradas"

**Solución:**

```bash
# Verifica que .env existe
ls -la minimarket_backend/.env

# Verifica que contiene valores reales (no dummy)
cat minimarket_backend/.env | grep MERCADOPAGO_ACCESS_TOKEN

# Si está vacío, edítalo:
# nano minimarket_backend/.env
```

### Frontend no conecta al backend: "API URL es /api"

**Solución:**

```bash
# Verifica que .env existe
ls -la minimarket_frontend/.env

# Verifica VITE_API_URL
cat minimarket_frontend/.env | grep VITE_API_URL

# Debe ser:
# VITE_API_URL=http://localhost:8080 (desarrollo)
# o
# VITE_API_URL=https://api.tu-dominio.com (producción)
```

### Variables de entorno no se cargan en producción

**Solución:**
Verifica que los archivos `.env` existan en el servidor:

```bash
# En servidor de producción
ssh user@server
cd /path/to/minimarket
ls -la minimarket_backend/.env
ls -la minimarket_frontend/.env

# Si no existen, crearlos con credenciales de producción
```

---

## 📚 REFERENCIAS

- **Spring DotEnv:** https://github.com/paulschwarz/spring-dotenv
- **Vite Environment Variables:** https://vitejs.dev/guide/env-and-mode.html
- **Best Practices Environment Variables:** https://12factor.net/config

---

## 📊 GIT COMMITS REALIZADOS

```
7731d18 FASE 3: Configurar Frontend para variables de entorno
8041020 FIX: Permitir .env.example en git pero bloquear .env
782b187 FASE 2: Configurar Backend para variables de entorno
005a80c SECURITY: Bloquear .env en gitignore para proteger credenciales
```

Para ver detalles:

```bash
git log --oneline | grep -E "FASE|SECURITY|FIX"
```

---

**¡Refactorización completada! 🎉**

Tu proyecto ahora:

- ✅ Mantiene todas las credenciales seguras fuera de Git
- ✅ Soporta múltiples ambientes (desarrollo, staging, producción)
- ✅ Es fácil de desplegar sin exponer secretos
- ✅ Sigue mejores prácticas de seguridad

Próximos pasos:

1. Editar `.env` local con tus credenciales reales
2. Editar `.env` de backend con contraseña de PostgreSQL real
3. Testear que backend y frontend se conectan
4. Desplegar a producción usando el mismo pattern
