package com.cashi.auth.controller;

import com.cashi.auth.dto.request.RolRequest;
import com.cashi.auth.dto.response.RolResponse;
import com.cashi.auth.service.RolManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Roles", description = "Endpoints para administración de roles y asignaciones")
public class RolManagementController {

    private final RolManagementService rolManagementService;

    @GetMapping
    @Operation(summary = "Obtener todos los roles")
    public ResponseEntity<List<RolResponse>> obtenerTodosRoles() {
        log.info("Solicitud para obtener todos los roles");
        List<RolResponse> roles = rolManagementService.obtenerTodosRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<RolResponse> obtenerRolPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener rol con ID: {}", id);
        RolResponse rol = rolManagementService.obtenerRolPorId(id);
        return ResponseEntity.ok(rol);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo rol")
    public ResponseEntity<RolResponse> crearRol(@Valid @RequestBody RolRequest request) {
        log.info("Solicitud para crear nuevo rol: {}", request.getNombreRol());
        RolResponse response = rolManagementService.crearRol(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol existente")
    public ResponseEntity<RolResponse> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request
    ) {
        log.info("Solicitud para actualizar rol con ID: {}", id);
        RolResponse response = rolManagementService.actualizarRol(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<Map<String, String>> eliminarRol(@PathVariable Long id) {
        log.info("Solicitud para eliminar rol con ID: {}", id);
        rolManagementService.eliminarRol(id);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Rol eliminado exitosamente");
        return ResponseEntity.ok(response);
    }
}
