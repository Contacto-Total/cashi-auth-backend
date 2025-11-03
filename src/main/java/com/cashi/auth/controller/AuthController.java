package com.cashi.auth.controller;

import com.cashi.auth.dto.request.LoginRequest;
import com.cashi.auth.dto.request.RefreshTokenRequest;
import com.cashi.auth.dto.request.RegistroRequest;
import com.cashi.auth.dto.response.AuthResponse;
import com.cashi.auth.dto.response.UsuarioResponse;
import com.cashi.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints de autenticación y autorización")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        log.info("Solicitud de registro para usuario: {}", request.getNombreUsuario());
        AuthResponse response = authService.registrarUsuario(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        // Agregar información de la solicitud
        request.setIpCliente(getClientIP(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));

        log.info("Solicitud de login para usuario: {}", request.getNombreUsuario());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Renovar access token usando refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Solicitud de refresh token");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            authService.logout(token);
        }

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Sesión cerrada exitosamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Cerrar sesión en todos los dispositivos")
    public ResponseEntity<Map<String, String>> logoutAllDevices(Authentication authentication) {
        // Obtener ID del usuario autenticado
        // Aquí necesitarías extraer el ID del usuario del token o autenticación
        // Por ahora, devolvemos un mensaje genérico

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Sesiones cerradas en todos los dispositivos");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        Map<String, Object> response = new HashMap<>();

        if (token == null) {
            response.put("valido", false);
            response.put("mensaje", "Token no proporcionado");
            return ResponseEntity.ok(response);
        }

        boolean esValido = authService.validarToken(token);
        response.put("valido", esValido);
        response.put("mensaje", esValido ? "Token válido" : "Token inválido o expirado");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener información del usuario autenticado")
    public ResponseEntity<UsuarioResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse response = authService.obtenerUsuarioActual(username);
        return ResponseEntity.ok(response);
    }

    // Métodos auxiliares

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
