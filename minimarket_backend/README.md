



mvn spring-boot:run

http://localhost:8080/api/public/status

http://localhost:8080/api/public/health




# 🛒 Minimarket Backend

Sistema de gestión para minimarket desarrollado con Spring Boot 3.2.4 y PostgreSQL.

## 📋 Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL 12 o superior
- IDE (vscode, antigravity)

## 🚀 Configuración Inicial

### 1. Crear Base de Datos

```sql
-- Conectarse a PostgreSQL
psql -U postgres

-- Crear la base de datos
CREATE DATABASE minimarket_db;

-- Verificar
\l
```

### 2. Configurar Credenciales

Editar `src/main/resources/application.yml` y ajustar las credenciales de tu base de datos:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/minimarket_db
    username: postgres          # Tu usuario de PostgreSQL
    password: john.007         # Tu contraseña de PostgreSQL
```

### 3. Instalar Dependencias

```bash
mvn clean install
```

## ▶️ Ejecutar la Aplicación

### Opción 1: Con Maven
```bash
mvn spring-boot:run
```

### Opción 2: Desde el IDE
Ejecutar la clase principal: `MinimarketApplication.java`

### Opción 3: JAR ejecutable
```bash
mvn clean package
java -jar target/minimarket-backend-1.0.0.jar
```

## 🔍 Verificar Funcionamiento

Una vez iniciada la aplicación, verás este mensaje en la consola:

```
=========================================
🚀 MINIMARKET BACKEND INICIADO CORRECTAMENTE
=========================================
🌐 URL: http://localhost:8080/api
📊 Estado: http://localhost:8080/api/public/status
🔑 Login: POST http://localhost:8080/api/auth/signin
   Usuario: admin
   Contraseña: admin123
=========================================
```

### Endpoints de Prueba

1. **Estado del servicio:**
```bash
curl http://localhost:8080/api/public/status
```

2. **Health Check:**
```bash
curl http://localhost:8080/api/public/health
```

3. **Verificar Base de Datos:**
```bash
curl http://localhost:8080/api/public/db-check
```

## 🔐 Autenticación

### Login

**Endpoint:** `POST http://localhost:8080/api/auth/signin`

**Body (JSON):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta exitosa:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "roles": ["ADMIN"]
}
```

### Usar el Token

Para endpoints protegidos, incluir el token en el header:

```bash
curl -H "Authorization: Bearer TU_TOKEN_AQUI" \
     http://localhost:8080/api/test
```

## 📁 Estructura del Proyecto

```
minimarket_backend/
├── src/main/java/com/minimarket/
│   ├── config/              # Configuraciones
│   │   ├── DataLoader.java
│   │   └── DebugFilter.java
│   ├── controller/          # Controladores REST
│   │   ├── AuthController.java
│   │   └── TestController.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   ├── entity/              # Entidades JPA
│   │   ├── Usuario.java
│   │   ├── Role.java
│   │   └── ERole.java
│   ├── repository/          # Repositorios
│   │   ├── UsuarioRepository.java
│   │   └── RoleRepository.java
│   ├── security/            # Configuración de seguridad
│   │   ├── WebSecurityConfig.java
│   │   ├── jwt/
│   │   └── services/
│   └── MinimarketApplication.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

## 🔧 Tecnologías

- **Spring Boot 3.2.4** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL** - Base de datos
- **JWT (JJWT 0.11.5)** - Tokens de autenticación
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias

## 🎯 Características

- ✅ Autenticación JWT
- ✅ Roles y permisos (ADMIN, VENDEDOR, ALMACENERO)
- ✅ Configuración CORS
- ✅ Validación de datos
- ✅ Manejo de errores
- ✅ Logging configurado
- ✅ Base de datos PostgreSQL

## 🐛 Solución de Problemas

### Error: "Connection refused" a PostgreSQL

**Solución:**
1. Verificar que PostgreSQL esté corriendo:
```bash
# Windows
net start postgresql-x64-[version]

# Linux/Mac
sudo service postgresql start
```

2. Verificar puerto (por defecto 5432):
```bash
netstat -an | grep 5432
```

### Error: "Access denied" al login

**Solución:**
1. Verificar logs de la aplicación
2. Revisar que el usuario 'admin' existe en la base de datos
3. Verificar credenciales: `admin` / `admin123`

### Error: "Port 8080 already in use"

**Solución:**
Cambiar el puerto en `application.yml`:
```yaml
server:
  port: 8081
```

## 📝 Cambios Importantes Realizados

### Archivos Corregidos

1. **WebSecurityConfig.java**
   - ✅ Integrada configuración CORS
   - ✅ Agregada anotación `@EnableMethodSecurity`
   - ✅ Mejorado el bean de CORS
   - ✅ Simplificada configuración de seguridad

2. **JwtUtils.java**
   - ✅ Uso de `@Value` para configuración desde `application.yml`
   - ✅ Mejorado manejo de claves
   - ✅ Agregado mejor manejo de errores

3. **AuthController.java**
   - ✅ Obtención correcta del ID del usuario
   - ✅ Mejor manejo de excepciones
   - ✅ Logging mejorado
   - ✅ Endpoint de validación de token

4. **DataLoader.java**
   - ✅ Mejor manejo de errores
   - ✅ Verificación de existencia antes de crear
   - ✅ Logging más claro

5. **application.yml**
   - ✅ Configuración de logging optimizada
   - ✅ Configuración de pool de conexiones
   - ✅ Parámetros JWT externalizados

6. **CorsConfig.java**
   - ⚠️ **ELIMINAR** - Ya no es necesario (ahora está en WebSecurityConfig)

## 🔄 Próximos Pasos

1. Agregar más endpoints para gestión de productos
2. Implementar categorías y proveedores
3. Sistema de ventas e inventario
4. Reportes y estadísticas
5. Documentación con Swagger/OpenAPI

## 📞 Soporte

Si encuentras problemas, verifica:
1. Logs en `logs/minimarket-backend.log`
2. Logs en la consola
3. Estado de la base de datos con `/api/public/db-check`

---

**Versión:** 1.0.0  
**Última actualización:** Enero 2026