-- ====================================
-- CASHI AUTH SERVICE - DATABASE SCHEMA
-- Base de datos: cashi_auth_db
-- ====================================

-- Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150),
    telefono VARCHAR(20),
    extension_sip VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    verificado_email BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NULL,
    ultimo_acceso TIMESTAMP NULL,
    intentos_fallidos INT DEFAULT 0,
    bloqueado_hasta TIMESTAMP NULL,
    INDEX idx_nombre_usuario (nombre_usuario),
    INDEX idx_email (email),
    INDEX idx_activo (activo),
    INDEX idx_extension_sip (extension_sip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: roles
CREATE TABLE IF NOT EXISTS roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nombre_rol (nombre_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: permisos
CREATE TABLE IF NOT EXISTS permisos (
    id_permiso BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    modulo VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nombre_permiso (nombre_permiso),
    INDEX idx_modulo (modulo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuarios_roles (relación Many-to-Many)
CREATE TABLE IF NOT EXISTS usuarios_roles (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: roles_permisos (relación Many-to-Many)
CREATE TABLE IF NOT EXISTS roles_permisos (
    id_rol BIGINT NOT NULL,
    id_permiso BIGINT NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE,
    FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: tokens (persistencia de JWT)
CREATE TABLE IF NOT EXISTS tokens (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    tipo_token VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    revocado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    ip_cliente VARCHAR(45),
    user_agent VARCHAR(255),
    dispositivo VARCHAR(100),
    fecha_revocacion TIMESTAMP NULL,
    id_usuario BIGINT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_usuario_activo (id_usuario, activo),
    INDEX idx_fecha_expiracion (fecha_expiracion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: sesiones_usuario (tracking de sesiones)
CREATE TABLE IF NOT EXISTS sesiones_usuario (
    id_sesion BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    id_usuario BIGINT NOT NULL,
    ip_cliente VARCHAR(45),
    user_agent VARCHAR(255),
    dispositivo VARCHAR(100),
    ubicacion VARCHAR(100),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_acceso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario_activa (id_usuario, activa),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
