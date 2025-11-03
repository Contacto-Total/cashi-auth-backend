-- =============================================
-- DATOS INICIALES - CASHI AUTH SERVICE
-- =============================================

-- Insertar Roles
INSERT IGNORE INTO roles (nombre_rol, descripcion, activo) VALUES
('ADMIN', 'Administrador del sistema con acceso completo', TRUE),
('SUPERVISOR', 'Supervisor de call center y WhatsApp', TRUE),
('AGENTE', 'Agente de call center o WhatsApp', TRUE),
('COORDINADOR', 'Coordinador de equipos', TRUE);

-- Insertar Permisos - MÓDULO DISCADOR
INSERT IGNORE INTO permisos (nombre_permiso, descripcion, modulo, activo) VALUES
('CALL_CREATE', 'Crear llamadas', 'DISCADOR', TRUE),
('CALL_READ', 'Ver llamadas', 'DISCADOR', TRUE),
('CALL_UPDATE', 'Actualizar llamadas', 'DISCADOR', TRUE),
('CALL_DELETE', 'Eliminar llamadas', 'DISCADOR', TRUE),
('CAMPAIGN_CREATE', 'Crear campañas', 'DISCADOR', TRUE),
('CAMPAIGN_READ', 'Ver campañas', 'DISCADOR', TRUE),
('CAMPAIGN_UPDATE', 'Actualizar campañas', 'DISCADOR', TRUE),
('CAMPAIGN_DELETE', 'Eliminar campañas', 'DISCADOR', TRUE),
('CAMPAIGN_MANAGE', 'Gestionar campañas (start/pause/stop)', 'DISCADOR', TRUE),
('AGENT_MANAGE', 'Gestionar agentes', 'DISCADOR', TRUE),
('CONTACT_IMPORT', 'Importar contactos', 'DISCADOR', TRUE),
('REPORT_VIEW', 'Ver reportes', 'DISCADOR', TRUE);

-- Insertar Permisos - MÓDULO WHATSAPP
INSERT IGNORE INTO permisos (nombre_permiso, descripcion, modulo, activo) VALUES
('WHATSAPP_SEND', 'Enviar mensajes de WhatsApp', 'WHATSAPP', TRUE),
('WHATSAPP_READ', 'Leer mensajes de WhatsApp', 'WHATSAPP', TRUE),
('WHATSAPP_MEDIA', 'Enviar archivos multimedia', 'WHATSAPP', TRUE),
('WHATSAPP_SESSION_MANAGE', 'Gestionar sesiones de WhatsApp', 'WHATSAPP', TRUE),
('WHATSAPP_CONTACT_SYNC', 'Sincronizar contactos', 'WHATSAPP', TRUE);

-- Insertar Permisos - MÓDULO ADMIN
INSERT IGNORE INTO permisos (nombre_permiso, descripcion, modulo, activo) VALUES
('USER_CREATE', 'Crear usuarios', 'ADMIN', TRUE),
('USER_READ', 'Ver usuarios', 'ADMIN', TRUE),
('USER_UPDATE', 'Actualizar usuarios', 'ADMIN', TRUE),
('USER_DELETE', 'Eliminar usuarios', 'ADMIN', TRUE),
('ROLE_MANAGE', 'Gestionar roles y permisos', 'ADMIN', TRUE),
('SYSTEM_CONFIG', 'Configurar sistema', 'ADMIN', TRUE);

-- Asignar Permisos a Roles

-- ADMIN: Todos los permisos
INSERT IGNORE INTO roles_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r, permisos p
WHERE r.nombre_rol = 'ADMIN';

-- SUPERVISOR: Permisos de gestión de campañas y reportes
INSERT IGNORE INTO roles_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r, permisos p
WHERE r.nombre_rol = 'SUPERVISOR'
AND p.nombre_permiso IN (
    'CALL_READ', 'CALL_UPDATE',
    'CAMPAIGN_READ', 'CAMPAIGN_UPDATE', 'CAMPAIGN_MANAGE',
    'AGENT_MANAGE', 'CONTACT_IMPORT', 'REPORT_VIEW',
    'WHATSAPP_SEND', 'WHATSAPP_READ', 'WHATSAPP_MEDIA', 'WHATSAPP_CONTACT_SYNC',
    'USER_READ'
);

-- AGENTE: Permisos básicos de llamadas y mensajes
INSERT IGNORE INTO roles_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r, permisos p
WHERE r.nombre_rol = 'AGENTE'
AND p.nombre_permiso IN (
    'CALL_CREATE', 'CALL_READ', 'CALL_UPDATE',
    'CAMPAIGN_READ',
    'WHATSAPP_SEND', 'WHATSAPP_READ', 'WHATSAPP_MEDIA'
);

-- COORDINADOR: Entre supervisor y agente
INSERT IGNORE INTO roles_permisos (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM roles r, permisos p
WHERE r.nombre_rol = 'COORDINADOR'
AND p.nombre_permiso IN (
    'CALL_CREATE', 'CALL_READ', 'CALL_UPDATE',
    'CAMPAIGN_READ', 'CAMPAIGN_UPDATE',
    'CONTACT_IMPORT', 'REPORT_VIEW',
    'WHATSAPP_SEND', 'WHATSAPP_READ', 'WHATSAPP_MEDIA', 'WHATSAPP_CONTACT_SYNC'
);

-- Usuario Admin por defecto
-- Contraseña: admin123 (encriptada con BCrypt)
INSERT IGNORE INTO usuarios (nombre_usuario, email, contrasena, nombre_completo, activo, verificado_email)
VALUES ('admin', 'admin@cashi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador del Sistema', TRUE, TRUE);

-- Asignar rol ADMIN al usuario admin
INSERT IGNORE INTO usuarios_roles (id_usuario, id_rol)
SELECT u.id_usuario, r.id_rol
FROM usuarios u, roles r
WHERE u.nombre_usuario = 'admin' AND r.nombre_rol = 'ADMIN';
