package com.cashi.auth.config;

import com.cashi.auth.entity.Permiso;
import com.cashi.auth.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeder para cargar el catálogo maestro de permisos del sistema
 * Organizado por categorías para facilitar la asignación en roles
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class PermisoSeeder implements CommandLineRunner {

    private final PermisoRepository permisoRepository;

    @Override
    public void run(String... args) {
        log.info("====================================================================");
        log.info("INICIANDO SEEDING - PERMISOS DEL SISTEMA");
        log.info("====================================================================");

        long permisosAntes = permisoRepository.count();
        log.info("Permisos existentes antes del seeding: {}", permisosAntes);

        seedPermisosClientes();
        seedPermisosGestiones();
        seedPermisosPagos();
        seedPermisosPromesas();
        seedPermisosAsignacion();
        seedPermisosReportes();
        seedPermisosConfiguracion();
        seedPermisosComunicaciones();

        long totalPermisos = permisoRepository.count();
        long permisosNuevos = totalPermisos - permisosAntes;

        log.info("====================================================================");
        if (permisosNuevos > 0) {
            log.info("✓ SEEDING COMPLETADO - {} NUEVOS PERMISOS AGREGADOS", permisosNuevos);
        } else {
            log.info("✓ SEEDING COMPLETADO - TODOS LOS PERMISOS YA EXISTEN");
        }
        log.info("  Total de permisos en BD: {}", totalPermisos);
        log.info("====================================================================");
    }

    private void seedPermisosClientes() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - CLIENTES...");

        int orden = 1;
        seedPermiso("CLIENTES_VER_ASIGNADOS", "Ver clientes asignados",
                "Ver solo los clientes de su cartera asignada",
                "CLIENTES", orden++);

        seedPermiso("CLIENTES_VER_TODOS", "Ver todos los clientes",
                "Ver clientes de toda la cartera/equipo",
                "CLIENTES", orden++);

        seedPermiso("CLIENTES_EDITAR", "Editar datos de cliente",
                "Modificar información del cliente",
                "CLIENTES", orden++);

        seedPermiso("CLIENTES_ELIMINAR", "Eliminar clientes",
                "Eliminar registros de clientes",
                "CLIENTES", orden++);

        seedPermiso("CLIENTES_EXPORTAR", "Exportar clientes",
                "Exportar información de clientes a Excel/CSV",
                "CLIENTES", orden++);

        log.info("✓ Permisos CLIENTES - {} permisos creados", orden - 1);
    }

    private void seedPermisosGestiones() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - GESTIONES...");

        int orden = 1;
        seedPermiso("GESTIONES_CREAR", "Crear gestiones",
                "Registrar nuevas gestiones de cobranza",
                "GESTIONES", orden++);

        seedPermiso("GESTIONES_EDITAR_PROPIAS", "Editar gestiones propias",
                "Modificar sus propias gestiones",
                "GESTIONES", orden++);

        seedPermiso("GESTIONES_EDITAR_TODAS", "Editar todas las gestiones",
                "Modificar gestiones de otros usuarios",
                "GESTIONES", orden++);

        seedPermiso("GESTIONES_ELIMINAR", "Eliminar gestiones",
                "Eliminar registros de gestiones",
                "GESTIONES", orden++);

        seedPermiso("GESTIONES_VER_HISTORIAL", "Ver historial de gestiones",
                "Acceder al historial completo de gestiones",
                "GESTIONES", orden++);

        seedPermiso("GESTIONES_EXPORTAR", "Exportar gestiones",
                "Exportar historial de gestiones a Excel/CSV",
                "GESTIONES", orden++);

        log.info("✓ Permisos GESTIONES - {} permisos creados", orden - 1);
    }

    private void seedPermisosPagos() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - PAGOS...");

        int orden = 1;
        seedPermiso("PAGOS_REGISTRAR", "Registrar pagos",
                "Registrar pagos recibidos de clientes",
                "PAGOS", orden++);

        seedPermiso("PAGOS_APROBAR", "Aprobar pagos",
                "Validar y aprobar pagos registrados",
                "PAGOS", orden++);

        seedPermiso("PAGOS_CANCELAR", "Cancelar pagos",
                "Anular pagos registrados",
                "PAGOS", orden++);

        seedPermiso("PAGOS_VER_TODOS", "Ver todos los pagos",
                "Ver pagos de todos los clientes",
                "PAGOS", orden++);

        seedPermiso("PAGOS_EXPORTAR", "Exportar pagos",
                "Exportar información de pagos a Excel/CSV",
                "PAGOS", orden++);

        log.info("✓ Permisos PAGOS - {} permisos creados", orden - 1);
    }

    private void seedPermisosPromesas() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - PROMESAS DE PAGO...");

        int orden = 1;
        seedPermiso("PROMESAS_CREAR", "Crear promesas de pago",
                "Registrar promesas de pago de clientes",
                "PROMESAS", orden++);

        seedPermiso("PROMESAS_APROBAR", "Aprobar promesas",
                "Aprobar promesas de pago de montos altos",
                "PROMESAS", orden++);

        seedPermiso("PROMESAS_CANCELAR", "Cancelar promesas",
                "Anular promesas de pago registradas",
                "PROMESAS", orden++);

        seedPermiso("PROMESAS_VER_TODAS", "Ver todas las promesas",
                "Ver promesas de pago de todos los clientes",
                "PROMESAS", orden++);

        log.info("✓ Permisos PROMESAS - {} permisos creados", orden - 1);
    }

    private void seedPermisosAsignacion() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - ASIGNACIÓN DE CARTERA...");

        int orden = 1;
        seedPermiso("ASIGNACION_REASIGNAR_EQUIPO", "Reasignar dentro del equipo",
                "Reasignar clientes entre asesores del mismo equipo",
                "ASIGNACION", orden++);

        seedPermiso("ASIGNACION_REASIGNAR_EQUIPOS", "Reasignar entre equipos",
                "Mover clientes entre diferentes equipos",
                "ASIGNACION", orden++);

        seedPermiso("ASIGNACION_MASIVA", "Asignación masiva",
                "Realizar asignaciones masivas de carteras",
                "ASIGNACION", orden++);

        log.info("✓ Permisos ASIGNACION - {} permisos creados", orden - 1);
    }

    private void seedPermisosReportes() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - REPORTES...");

        int orden = 1;
        seedPermiso("REPORTES_VER_PROPIOS", "Ver reportes propios",
                "Ver solo sus propias métricas y estadísticas",
                "REPORTES", orden++);

        seedPermiso("REPORTES_VER_EQUIPO", "Ver reportes de equipo",
                "Ver métricas y estadísticas del equipo",
                "REPORTES", orden++);

        seedPermiso("REPORTES_VER_GLOBALES", "Ver reportes globales",
                "Acceder a todos los reportes del sistema",
                "REPORTES", orden++);

        seedPermiso("REPORTES_EXPORTAR", "Exportar reportes",
                "Exportar reportes a Excel/PDF/CSV",
                "REPORTES", orden++);

        seedPermiso("REPORTES_DASHBOARD", "Acceso a dashboards",
                "Ver dashboards y tableros de control",
                "REPORTES", orden++);

        log.info("✓ Permisos REPORTES - {} permisos creados", orden - 1);
    }

    private void seedPermisosConfiguracion() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - CONFIGURACIÓN...");

        int orden = 1;
        seedPermiso("CONFIG_USUARIOS_GESTIONAR", "Gestionar usuarios",
                "Crear, editar y eliminar usuarios del sistema",
                "CONFIGURACION", orden++);

        seedPermiso("CONFIG_ROLES_GESTIONAR", "Gestionar roles",
                "Configurar roles y asignar permisos",
                "CONFIGURACION", orden++);

        seedPermiso("CONFIG_CATALOGOS_GESTIONAR", "Gestionar catálogos",
                "Administrar catálogos y listas del sistema",
                "CONFIGURACION", orden++);

        seedPermiso("CONFIG_AUDITORIA_VER", "Ver logs de auditoría",
                "Acceder a registros de auditoría del sistema",
                "CONFIGURACION", orden++);

        seedPermiso("CONFIG_PARAMETROS_EDITAR", "Editar parámetros del sistema",
                "Modificar configuraciones generales del sistema",
                "CONFIGURACION", orden++);

        seedPermiso("CONFIG_CABECERAS_GESTIONAR", "Gestionar cabeceras",
                "Configurar mapeo de cabeceras de archivos",
                "CONFIGURACION", orden++);

        log.info("✓ Permisos CONFIGURACION - {} permisos creados", orden - 1);
    }

    private void seedPermisosComunicaciones() {
        log.info("--------------------------------------------------------------------");
        log.info("Seeding Permisos - COMUNICACIONES...");

        int orden = 1;
        seedPermiso("COMUNICACIONES_SMS_ENVIAR", "Enviar SMS",
                "Enviar mensajes SMS a clientes",
                "COMUNICACIONES", orden++);

        seedPermiso("COMUNICACIONES_EMAIL_ENVIAR", "Enviar emails",
                "Enviar correos electrónicos a clientes",
                "COMUNICACIONES", orden++);

        seedPermiso("COMUNICACIONES_LLAMADAS_REALIZAR", "Realizar llamadas",
                "Hacer llamadas desde el discador integrado",
                "COMUNICACIONES", orden++);

        seedPermiso("COMUNICACIONES_GRABACIONES_ESCUCHAR", "Escuchar grabaciones",
                "Acceder a grabaciones de llamadas",
                "COMUNICACIONES", orden++);

        seedPermiso("COMUNICACIONES_MODO_SUSURRO", "Modo susurro/tripartita",
                "Entrar en modo susurro o llamada tripartita",
                "COMUNICACIONES", orden++);

        seedPermiso("COMUNICACIONES_CAMPANAS_GESTIONAR", "Gestionar campañas",
                "Crear y administrar campañas de comunicación",
                "COMUNICACIONES", orden++);

        log.info("✓ Permisos COMUNICACIONES - {} permisos creados", orden - 1);
    }

    private void seedPermiso(String codigo, String nombre, String descripcion,
                            String categoria, int orden) {
        if (!permisoRepository.existsByCodigoPermiso(codigo)) {
            Permiso permiso = new Permiso(
                    codigo, nombre, descripcion, categoria, orden
            );
            permisoRepository.save(permiso);
            log.debug("  ✓ {} - {}", codigo, nombre);
        }
    }
}
