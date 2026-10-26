package com.cashi.auth.service;

import com.cashi.auth.exception.AccesoInhabilitadoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Si RR.HH. inhabilito a un usuario: un descanso o licencia registrado en
 * Asistencia, sus vacaciones, o un dia «Sin asignacion» del calendario de su
 * subcartera. Todo vive en cashi_discador_db.
 *
 * Se revisa al iniciar sesion y al renovar el token, ANTES de crear la sesion:
 * el discador toma la entrada del dia del primer token, y un ingreso
 * bloqueado no debe contar como asistencia.
 *
 * Va con JdbcTemplate y no con el EntityManager a proposito: si la consulta
 * falla (por ejemplo, falta el permiso sobre la otra base), un error de JPA
 * marcaria la transaccion del login para rollback y nadie podria entrar. Si
 * falla, se deja pasar y se registra el error.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InhabilitacionAccesoService {

    /** Las fechas del discador estan en hora de Lima; el auth no fija su zona. */
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM", ES);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("h:mm a", ES);

    private static final String CIERRE = " Por ahora no puedes ingresar a Cashi. Si necesitas algo o quieres "
            + "volver antes, comunícate con tu supervisor(a).";

    private final JdbcTemplate jdbc;

    /** Lanza {@link AccesoInhabilitadoException} si hoy no puede ingresar. */
    public void exigirHabilitado(Long idUsuario) {
        String mensaje;
        try {
            mensaje = motivoDeBloqueo(idUsuario);
        } catch (Exception e) {
            log.error("No se pudo revisar si el usuario {} está inhabilitado; se deja ingresar", idUsuario, e);
            return;
        }
        if (mensaje != null) {
            log.info("Ingreso bloqueado para el usuario {}: inhabilitado por RR.HH.", idUsuario);
            throw new AccesoInhabilitadoException(mensaje);
        }
    }

    private String motivoDeBloqueo(Long idUsuario) {
        LocalDateTime ahora = LocalDateTime.now(LIMA);

        List<Object[]> inhabilitaciones = jdbc.query("""
                SELECT hasta, id_justificacion
                FROM cashi_discador_db.asistencia_inhabilitacion
                WHERE id_usuario = ? AND estado = 'ACTIVA' AND desde <= ? AND hasta > ?
                ORDER BY hasta DESC
                LIMIT 1
                """,
                (rs, n) -> new Object[]{rs.getTimestamp("hasta"), rs.getObject("id_justificacion")},
                idUsuario, Timestamp.valueOf(ahora), Timestamp.valueOf(ahora));
        if (!inhabilitaciones.isEmpty()) {
            LocalDateTime hasta = ((Timestamp) inhabilitaciones.get(0)[0]).toLocalDateTime();
            boolean licencia = inhabilitaciones.get(0)[1] != null;
            return (licencia ? "Tienes una licencia registrada hasta " : "Tienes un descanso registrado hasta ")
                    + cuando(hasta) + "." + CIERRE;
        }

        List<LocalDate> vacaciones = jdbc.query("""
                SELECT fecha_hasta
                FROM cashi_discador_db.asistencia_vacaciones
                WHERE id_usuario = ? AND fecha_desde <= ? AND fecha_hasta >= ?
                ORDER BY fecha_hasta DESC
                LIMIT 1
                """,
                (rs, n) -> rs.getDate("fecha_hasta").toLocalDate(),
                idUsuario, java.sql.Date.valueOf(ahora.toLocalDate()), java.sql.Date.valueOf(ahora.toLocalDate()));
        if (!vacaciones.isEmpty()) {
            return "Estás de vacaciones hasta el " + vacaciones.get(0).format(DIA) + "." + CIERRE;
        }

        // Dia «Sin asignacion» del Calendario de Asistencia para su subcartera de
        // hoy. Como en el calendario, el dia marcado para la subcartera pisa al
        // de toda la empresa: si la subcartera tiene otro tipo ese dia, no bloquea.
        List<Integer> sinAsignacion = jdbc.query("""
                SELECT 1
                FROM cashi_discador_db.personal p
                JOIN cashi_discador_db.personal_subcartera ps
                  ON ps.id_personal = p.id
                 AND ps.fecha_desde <= ? AND (ps.fecha_hasta IS NULL OR ps.fecha_hasta >= ?)
                JOIN cashi_discador_db.dia_calendario d
                  ON d.fecha = ?
                 AND (d.id_subcartera = ps.id_subcartera
                      OR (d.id_subcartera IS NULL AND NOT EXISTS (
                            SELECT 1 FROM cashi_discador_db.dia_calendario d2
                            WHERE d2.fecha = d.fecha AND d2.id_subcartera = ps.id_subcartera)))
                JOIN cashi_discador_db.tipo_dia t ON t.id = d.id_tipo_dia AND t.codigo = 'SIN_ASIGNACION'
                WHERE p.id_usuario = ?
                LIMIT 1
                """,
                (rs, n) -> 1,
                java.sql.Date.valueOf(ahora.toLocalDate()), java.sql.Date.valueOf(ahora.toLocalDate()),
                java.sql.Date.valueOf(ahora.toLocalDate()), idUsuario);
        if (!sinAsignacion.isEmpty()) {
            return "Hoy " + ahora.toLocalDate().format(DIA) + " no tienes cartera asignada." + CIERRE;
        }
        return null;
    }

    /** «el 26/10 a las 6:00 p. m.»; si termina a medianoche, basta el ultimo dia: «el 27/10». */
    private static String cuando(LocalDateTime hasta) {
        if (hasta.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return "el " + hasta.toLocalDate().minusDays(1).format(DIA);
        }
        return "el " + hasta.format(DIA) + " a las " + hasta.format(HORA);
    }
}
