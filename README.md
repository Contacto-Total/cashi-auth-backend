# CASHI AUTH SERVICE

Servicio de autenticación centralizado para la plataforma Cashi.

## Tecnologías

- Java 21
- Spring Boot 3.2.0
- Spring Security
- JWT (JSON Web Tokens)
- MySQL 8.0
- Hibernate/JPA
- Lombok
- Swagger/OpenAPI

## Características

- ✅ Autenticación con JWT (Access Token + Refresh Token)
- ✅ Persistencia de tokens en base de datos
- ✅ Sistema de roles y permisos (RBAC)
- ✅ Tracking de sesiones de usuario
- ✅ Bloqueo automático por intentos fallidos
- ✅ Limpieza automática de tokens expirados
- ✅ CORS configurado
- ✅ Swagger UI integrado
- ✅ Base de datos en español

## Arquitectura

### Base de Datos (5 tablas principales)

1. **usuarios** - Información de usuarios
2. **roles** - Roles del sistema (ADMIN, SUPERVISOR, AGENTE, etc.)
3. **permisos** - Permisos granulares por módulo
4. **tokens** - Persistencia de JWT (access + refresh)
5. **sesiones_usuario** - Tracking de sesiones activas

### Roles por Defecto

- **ADMIN**: Acceso completo al sistema
- **SUPERVISOR**: Gestión de campañas y reportes
- **AGENTE**: Permisos básicos de operación
- **COORDINADOR**: Entre supervisor y agente

### Permisos por Módulo

- **DISCADOR**: CALL_*, CAMPAIGN_*, AGENT_MANAGE, etc.
- **WHATSAPP**: WHATSAPP_SEND, WHATSAPP_READ, etc.
- **ADMIN**: USER_*, ROLE_MANAGE, SYSTEM_CONFIG

## Instalación

### 1. Crear base de datos

```sql
CREATE DATABASE cashi_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configurar application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cashi_auth_db
spring.datasource.username=root
spring.datasource.password=root
```

### 3. Ejecutar aplicación

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación iniciará en: **http://localhost:8082**

## Endpoints

### Autenticación (Públicos)

- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/refresh-token` - Renovar access token
- `GET /api/auth/validate` - Validar token

### Protegidos (Requieren JWT)

- `GET /api/auth/me` - Obtener usuario actual
- `POST /api/auth/logout` - Cerrar sesión
- `POST /api/auth/logout-all` - Cerrar todas las sesiones

## Documentación API

Swagger UI: **http://localhost:8082/swagger-ui.html**

## Usuario por Defecto

```
Usuario: admin
Contraseña: admin123
Rol: ADMIN
```

## Flujo de Autenticación

1. **Login**: `POST /api/auth/login`
   - Retorna: `accessToken` + `refreshToken`
   - Access token expira en 24 horas
   - Refresh token expira en 7 días

2. **Usar Access Token**:
   ```
   Authorization: Bearer {accessToken}
   ```

3. **Renovar Token**: `POST /api/auth/refresh-token`
   - Cuando el access token expire
   - Usa el refresh token para obtener nuevo access token

4. **Logout**: `POST /api/auth/logout`
   - Revoca el token actual

## Integración con otros Microservicios

### Validar JWT en otros servicios

```java
// En otros microservicios (discador, whatsapp)
@GetMapping("/validar-token")
public Boolean validarToken(@RequestHeader("Authorization") String token) {
    String jwt = token.substring(7); // Remover "Bearer "

    // Llamar al auth-service
    ResponseEntity<Map> response = restTemplate.getForEntity(
        "http://localhost:8082/api/auth/validate?token=" + jwt,
        Map.class
    );

    return (Boolean) response.getBody().get("valido");
}
```

## Características de Seguridad

- ✅ Contraseñas encriptadas con BCrypt
- ✅ JWT firmados con HS512
- ✅ Bloqueo automático tras 5 intentos fallidos (30 minutos)
- ✅ Tokens revocables
- ✅ Limpieza automática de tokens expirados (diaria a las 2 AM)
- ✅ Tracking de IP, User-Agent y dispositivo
- ✅ CORS configurado para múltiples orígenes

## Variables de Entorno

```properties
JWT_SECRET=miClaveSecretaSuperSeguraParaJWT...
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8000
```

## Estructura del Proyecto

```
cashi-auth-backend/
├── src/main/java/com/cashi/auth/
│   ├── config/           # Configuraciones (Security, CORS)
│   ├── controller/       # Controllers REST
│   ├── dto/              # DTOs (request/response)
│   ├── entity/           # Entidades JPA
│   ├── repository/       # Repositorios JPA
│   ├── security/         # JWT Utils, Filters
│   ├── service/          # Lógica de negocio
│   └── CashiAuthApplication.java
├── src/main/resources/
│   ├── application.properties
│   ├── schema.sql        # Schema de BD
│   └── data.sql          # Datos iniciales
└── pom.xml
```

## Contribución

Este servicio es parte de la arquitectura de microservicios de CASHI.

## Licencia

Propietario - CASHI © 2024
