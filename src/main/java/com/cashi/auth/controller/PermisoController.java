package com.cashi.auth.controller;

import com.cashi.auth.dto.response.PermisoResponse;
import com.cashi.auth.service.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Permisos", description = "Catálogo maestro de permisos disponibles organizados por categorías")
public class PermisoController {

    private final PermisoService permisoService;

    @GetMapping
    @Operation(summary = "Obtener todos los permisos disponibles")
    public ResponseEntity<List<PermisoResponse>> obtenerTodosPermisos() {
        log.info("Solicitud para obtener todos los permisos");
        List<PermisoResponse> permisos = permisoService.obtenerTodosPermisos();
        return ResponseEntity.ok(permisos);
    }

    @GetMapping("/agrupados")
    @Operation(summary = "Obtener permisos agrupados por categoría")
    public ResponseEntity<Map<String, List<PermisoResponse>>> obtenerPermisosAgrupados() {
        log.info("Solicitud para obtener permisos agrupados por categoría");
        Map<String, List<PermisoResponse>> permisosAgrupados =
                permisoService.obtenerPermisosAgrupadosPorCategoria();
        return ResponseEntity.ok(permisosAgrupados);
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Obtener permisos de una categoría específica")
    public ResponseEntity<List<PermisoResponse>> obtenerPermisosPorCategoria(
            @PathVariable String categoria) {
        log.info("Solicitud para obtener permisos de la categoría: {}", categoria);
        List<PermisoResponse> permisos =
                permisoService.obtenerPermisosPorCategoria(categoria);
        return ResponseEntity.ok(permisos);
    }
}
