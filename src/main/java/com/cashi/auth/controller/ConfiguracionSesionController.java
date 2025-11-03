package com.cashi.auth.controller;

import com.cashi.auth.service.ConfiguracionSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion/sesion")
@RequiredArgsConstructor
public class ConfiguracionSesionController {

    private final ConfiguracionSesionService configuracionService;

    /**
     * Obtener todas las configuraciones de sesión
     * Este endpoint es público para que el frontend pueda obtener los tiempos de timeout
     */
    @GetMapping
    public ResponseEntity<Map<String, Integer>> obtenerConfiguraciones() {
        return ResponseEntity.ok(configuracionService.obtenerTodasLasConfiguraciones());
    }

    /**
     * Obtener una configuración específica
     */
    @GetMapping("/{clave}")
    public ResponseEntity<Integer> obtenerConfiguracion(@PathVariable String clave) {
        return ResponseEntity.ok(configuracionService.obtenerValor(clave));
    }

    /**
     * Actualizar una configuración (solo ADMIN)
     */
    @PutMapping("/{clave}")
    public ResponseEntity<String> actualizarConfiguracion(
            @PathVariable String clave,
            @RequestBody Map<String, Integer> body
    ) {
        Integer valor = body.get("valor");
        configuracionService.actualizarConfiguracion(clave, valor);
        return ResponseEntity.ok("Configuración actualizada exitosamente");
    }

    /**
     * Inicializar configuraciones por defecto (solo ADMIN)
     */
    @PostMapping("/inicializar")
    public ResponseEntity<String> inicializarConfiguraciones() {
        configuracionService.inicializarConfiguracionesPorDefecto();
        return ResponseEntity.ok("Configuraciones inicializadas exitosamente");
    }
}
