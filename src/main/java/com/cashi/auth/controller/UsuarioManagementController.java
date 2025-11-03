package com.cashi.auth.controller;

import com.cashi.auth.dto.request.UsuarioRequest;
import com.cashi.auth.dto.response.UsuarioResponse;
import com.cashi.auth.service.UsuarioManagementService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión de Usuarios", description = "Endpoints para administración de usuarios")
public class UsuarioManagementController {

    private final UsuarioManagementService usuarioManagementService;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodosUsuarios() {
        log.info("Solicitud para obtener todos los usuarios");
        List<UsuarioResponse> usuarios = usuarioManagementService.obtenerTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener usuario con ID: {}", id);
        UsuarioResponse usuario = usuarioManagementService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        log.info("Solicitud para crear nuevo usuario: {}", request.getNombreUsuario());
        UsuarioResponse response = usuarioManagementService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario existente")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request
    ) {
        log.info("Solicitud para actualizar usuario con ID: {}", id);
        UsuarioResponse response = usuarioManagementService.actualizarUsuario(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Long id) {
        log.info("Solicitud para eliminar usuario con ID: {}", id);
        usuarioManagementService.eliminarUsuario(id);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado exitosamente");
        return ResponseEntity.ok(response);
    }
}
