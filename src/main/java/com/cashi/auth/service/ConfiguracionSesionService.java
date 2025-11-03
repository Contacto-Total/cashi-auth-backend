package com.cashi.auth.service;

import com.cashi.auth.dto.ConfiguracionSesionDTO;
import com.cashi.auth.entity.ConfiguracionSesion;
import com.cashi.auth.repository.ConfiguracionSesionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionSesionService {

    private final ConfiguracionSesionRepository configuracionRepository;

    // Constantes de configuración
    public static final String TIMEOUT_INACTIVIDAD = "TIMEOUT_INACTIVIDAD"; // Tiempo de inactividad antes de cerrar sesión
    public static final String TIMEOUT_WARNING = "TIMEOUT_WARNING"; // Tiempo antes de mostrar advertencia
    public static final String ACCESS_TOKEN_EXPIRATION = "ACCESS_TOKEN_EXPIRATION"; // Duración del access token
    public static final String REFRESH_TOKEN_EXPIRATION = "REFRESH_TOKEN_EXPIRATION"; // Duración del refresh token
    public static final String AUTO_REFRESH_ENABLED = "AUTO_REFRESH_ENABLED"; // 1 = activado, 0 = desactivado

    // Valores por defecto (en segundos)
    private static final int DEFAULT_TIMEOUT_INACTIVIDAD = 900; // 15 minutos
    private static final int DEFAULT_TIMEOUT_WARNING = 60; // 1 minuto antes de cerrar
    private static final int DEFAULT_ACCESS_TOKEN_EXPIRATION = 3600; // 1 hora
    private static final int DEFAULT_REFRESH_TOKEN_EXPIRATION = 604800; // 7 días

    @Transactional(readOnly = true)
    public Integer obtenerValor(String clave) {
        return configuracionRepository.findByClaveAndActivoTrue(clave)
                .map(ConfiguracionSesion::getValor)
                .orElseGet(() -> obtenerValorPorDefecto(clave));
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> obtenerTodasLasConfiguraciones() {
        Map<String, Integer> configuraciones = new HashMap<>();

        configuraciones.put(TIMEOUT_INACTIVIDAD, obtenerValor(TIMEOUT_INACTIVIDAD));
        configuraciones.put(TIMEOUT_WARNING, obtenerValor(TIMEOUT_WARNING));
        configuraciones.put(ACCESS_TOKEN_EXPIRATION, obtenerValor(ACCESS_TOKEN_EXPIRATION));
        configuraciones.put(REFRESH_TOKEN_EXPIRATION, obtenerValor(REFRESH_TOKEN_EXPIRATION));
        configuraciones.put(AUTO_REFRESH_ENABLED, obtenerValor(AUTO_REFRESH_ENABLED));

        return configuraciones;
    }

    @Transactional
    public void actualizarConfiguracion(String clave, Integer valor) {
        ConfiguracionSesion config = configuracionRepository.findByClave(clave)
                .orElse(ConfiguracionSesion.builder()
                        .clave(clave)
                        .descripcion(obtenerDescripcion(clave))
                        .build());

        config.setValor(valor);
        configuracionRepository.save(config);

        log.info("Configuración actualizada: {} = {}", clave, valor);
    }

    @Transactional
    public void inicializarConfiguracionesPorDefecto() {
        crearSiNoExiste(TIMEOUT_INACTIVIDAD, DEFAULT_TIMEOUT_INACTIVIDAD,
            "Tiempo de inactividad en segundos antes de cerrar sesión automáticamente");

        crearSiNoExiste(TIMEOUT_WARNING, DEFAULT_TIMEOUT_WARNING,
            "Tiempo en segundos antes de mostrar advertencia de cierre de sesión");

        crearSiNoExiste(ACCESS_TOKEN_EXPIRATION, DEFAULT_ACCESS_TOKEN_EXPIRATION,
            "Duración del access token en segundos");

        crearSiNoExiste(REFRESH_TOKEN_EXPIRATION, DEFAULT_REFRESH_TOKEN_EXPIRATION,
            "Duración del refresh token en segundos");

        crearSiNoExiste(AUTO_REFRESH_ENABLED, 1,
            "Habilitar renovación automática de token (1=sí, 0=no)");

        log.info("Configuraciones de sesión inicializadas");
    }

    private void crearSiNoExiste(String clave, Integer valor, String descripcion) {
        if (!configuracionRepository.findByClave(clave).isPresent()) {
            ConfiguracionSesion config = ConfiguracionSesion.builder()
                    .clave(clave)
                    .valor(valor)
                    .descripcion(descripcion)
                    .activo(true)
                    .build();
            configuracionRepository.save(config);
        }
    }

    private Integer obtenerValorPorDefecto(String clave) {
        switch (clave) {
            case TIMEOUT_INACTIVIDAD:
                return DEFAULT_TIMEOUT_INACTIVIDAD;
            case TIMEOUT_WARNING:
                return DEFAULT_TIMEOUT_WARNING;
            case ACCESS_TOKEN_EXPIRATION:
                return DEFAULT_ACCESS_TOKEN_EXPIRATION;
            case REFRESH_TOKEN_EXPIRATION:
                return DEFAULT_REFRESH_TOKEN_EXPIRATION;
            case AUTO_REFRESH_ENABLED:
                return 1;
            default:
                return 0;
        }
    }

    private String obtenerDescripcion(String clave) {
        switch (clave) {
            case TIMEOUT_INACTIVIDAD:
                return "Tiempo de inactividad en segundos antes de cerrar sesión automáticamente";
            case TIMEOUT_WARNING:
                return "Tiempo en segundos antes de mostrar advertencia de cierre de sesión";
            case ACCESS_TOKEN_EXPIRATION:
                return "Duración del access token en segundos";
            case REFRESH_TOKEN_EXPIRATION:
                return "Duración del refresh token en segundos";
            case AUTO_REFRESH_ENABLED:
                return "Habilitar renovación automática de token (1=sí, 0=no)";
            default:
                return "";
        }
    }
}
