package com.cashi.auth.service;

import com.cashi.auth.dto.request.LoginRequest;
import com.cashi.auth.dto.request.RegistroRequest;
import com.cashi.auth.dto.request.RefreshTokenRequest;
import com.cashi.auth.dto.response.AuthResponse;
import com.cashi.auth.dto.response.UsuarioResponse;
import com.cashi.auth.entity.Rol;
import com.cashi.auth.entity.RolAsignacion;
import com.cashi.auth.entity.Token;
import com.cashi.auth.entity.Usuario;
import com.cashi.auth.repository.RolRepository;
import com.cashi.auth.repository.TokenRepository;
import com.cashi.auth.repository.UsuarioRepository;
import com.cashi.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.security.authentication.AuthenticationManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public AuthResponse registrarUsuario(RegistroRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getNombreUsuario());

        // Validar que no exista el usuario
        if (usuarioRepository.existsByNombreUsuario(request.getNombreUsuario())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario())
                .email(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .nombreCompleto(request.getNombreCompleto())
                .telefono(request.getTelefono())
                .extensionSip(request.getExtensionSip())
                .activo(true)
                .verificadoEmail(false)
                .build();

        // Asignar roles
        Set<Rol> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(rolNombre -> {
                Rol rol = rolRepository.findByNombreRol(rolNombre)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));
                roles.add(rol);
            });
        } else {
            // Rol por defecto: AGENTE
            Rol rolAgente = rolRepository.findByNombreRol("AGENTE")
                    .orElseThrow(() -> new RuntimeException("Rol AGENTE no encontrado"));
            roles.add(rolAgente);
        }
        usuario.setRoles(roles);

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente: {}", usuario.getNombreUsuario());

        // Generar tokens
        String accessToken = jwtUtil.generateAccessToken(usuario);
        String refreshToken = jwtUtil.generateRefreshToken(usuario);

        // Guardar tokens en BD
        tokenService.guardarToken(accessToken, usuario, Token.TipoToken.ACCESS, null, null, null);
        tokenService.guardarToken(refreshToken, usuario, Token.TipoToken.REFRESH, null, null, null);

        return construirAuthResponse(usuario, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para usuario: {}", request.getNombreUsuario());

        try {
            // Buscar usuario CON ROLES usando query nativa
            Usuario usuario;
            try {
                usuario = usuarioRepository.findByNombreUsuarioWithRoles(request.getNombreUsuario())
                        .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

                // Forzar inicialización de la colección
                entityManager.refresh(usuario);
                usuario.getRoles().size(); // Touch para inicializar
            } catch (BadCredentialsException e) {
                throw e;
            }

            // DEBUG: Verificar roles cargados
            System.out.println("=== DEBUG ROLES AFTER REFRESH ===");
            System.out.println("Usuario: " + usuario.getNombreUsuario());
            System.out.println("Roles count: " + usuario.getRoles().size());
            usuario.getRoles().forEach(rol -> System.out.println("  - Rol: " + rol.getNombreRol()));
            System.out.println("=== FIN DEBUG ===");

            // Verificar si está bloqueado
            if (!usuario.isAccountNonLocked()) {
                throw new RuntimeException("Cuenta bloqueada hasta: " + usuario.getBloqueadoHasta());
            }

            // Autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNombreUsuario(),
                            request.getContrasena()
                    )
            );

            // Login exitoso - resetear intentos fallidos
            usuario.resetearIntentosFallidos();
            usuario.actualizarUltimoAcceso();
            usuario = usuarioRepository.save(usuario);

            // IMPORTANTE: Limpiar contexto y recargar con roles
            entityManager.clear();
            usuario = usuarioRepository.findByNombreUsuarioWithRoles(request.getNombreUsuario())
                    .orElseThrow(() -> new RuntimeException("Error recargando usuario"));

            // Forzar inicialización de roles y permisos
            Hibernate.initialize(usuario.getRoles());
            usuario.getRoles().forEach(rol -> Hibernate.initialize(rol.getPermisos()));

            // DEBUG: Verificar roles después de recarga
            System.out.println("=== DEBUG DESPUÉS DE RECARGA ===");
            System.out.println("Usuario: " + usuario.getNombreUsuario());
            System.out.println("Roles count: " + usuario.getRoles().size());
            usuario.getRoles().forEach(rol -> System.out.println("  - Rol: " + rol.getNombreRol()));
            System.out.println("Authorities count: " + usuario.getAuthorities().size());
            usuario.getAuthorities().forEach(auth -> System.out.println("  - Authority: " + auth.getAuthority()));
            System.out.println("=== FIN DEBUG ===");
            
            // Generar tokens
            String accessToken = jwtUtil.generateAccessToken(usuario);
            String refreshToken = jwtUtil.generateRefreshToken(usuario);

            // Guardar tokens en BD
            tokenService.guardarToken(accessToken, usuario, Token.TipoToken.ACCESS,
                    request.getIpCliente(), request.getUserAgent(), request.getDispositivo());
            tokenService.guardarToken(refreshToken, usuario, Token.TipoToken.REFRESH,
                    request.getIpCliente(), request.getUserAgent(), request.getDispositivo());

            log.info("Login exitoso para usuario: {}", usuario.getNombreUsuario());

            return construirAuthResponse(usuario, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            // Incrementar intentos fallidos
            usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).ifPresent(usuario -> {
                usuario.incrementarIntentosFallidos();
                usuarioRepository.save(usuario);
            });
            log.warn("Login fallido para usuario: {}", request.getNombreUsuario());
            throw new RuntimeException("Credenciales inválidas");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        // Validar refresh token
        if (!jwtUtil.validateToken(refreshTokenStr)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        // Verificar que el token esté en la BD y sea válido
        Token refreshToken = tokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));

        if (!refreshToken.esValido()) {
            throw new RuntimeException("Refresh token inválido o revocado");
        }

        // Obtener usuario
        Usuario usuario = refreshToken.getUsuario();

        // Generar nuevo access token
        String nuevoAccessToken = jwtUtil.generateAccessToken(usuario);

        // Guardar nuevo access token
        tokenService.guardarToken(nuevoAccessToken, usuario, Token.TipoToken.ACCESS, null, null, null);

        log.info("Access token renovado para usuario: {}", usuario.getNombreUsuario());

        return construirAuthResponse(usuario, nuevoAccessToken, refreshTokenStr);
    }

    @Transactional
    public void logout(String accessToken) {
        tokenService.revocarToken(accessToken);
        log.info("Logout exitoso");
    }

    @Transactional
    public void logoutAllDevices(Long idUsuario) {
        tokenService.revocarTodosLosTokensDelUsuario(idUsuario);
        log.info("Logout de todos los dispositivos para usuario: {}", idUsuario);
    }

    @Transactional(readOnly = true)
    public boolean validarToken(String token) {
        return jwtUtil.validateToken(token) && tokenService.esTokenValido(token);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioActual(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return convertirAUsuarioResponse(usuario);
    }

    // Métodos auxiliares

    private AuthResponse construirAuthResponse(Usuario usuario, String accessToken, String refreshToken) {
        // DEBUG
        System.out.println("=== DEBUG construirAuthResponse ===");
        System.out.println("Usuario ID: " + usuario.getIdUsuario());
        System.out.println("Usuario nombre: " + usuario.getNombreUsuario());
        System.out.println("Roles size: " + usuario.getRoles().size());
        usuario.getRoles().forEach(rol -> System.out.println("Rol encontrado: " + rol.getNombreRol()));
        // FIN DEBUG

        Set<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombreRol)
                .collect(Collectors.toSet());

        // DEBUG
        System.out.println("Roles después del stream: " + roles);
        System.out.println("=== FIN DEBUG ===");

        Set<String> permisos = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        // Obtener asignación del rol (tenant/cartera/subcartera)
        Long tenantId = null;
        Long portfolioId = null;
        Long subPortfolioId = null;

        if (!usuario.getRoles().isEmpty()) {
            Rol primerRol = usuario.getRoles().iterator().next();
            if (!primerRol.getAsignaciones().isEmpty()) {
                RolAsignacion asignacion = primerRol.getAsignaciones().iterator().next();
                tenantId = asignacion.getTenantId();
                portfolioId = asignacion.getPortfolioId();
                subPortfolioId = asignacion.getSubPortfolioId();
            }
        }

        return AuthResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .extensionSip(usuario.getExtensionSip())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tipoToken("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .roles(roles)
                .permisos(permisos)
                .tenantId(tenantId)
                .portfolioId(portfolioId)
                .subPortfolioId(subPortfolioId)
                .mensaje("Autenticación exitosa")
                .build();
    }

    private UsuarioResponse convertirAUsuarioResponse(Usuario usuario) {
        Set<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombreRol)
                .collect(Collectors.toSet());

        Set<String> permisos = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .telefono(usuario.getTelefono())
                .extensionSip(usuario.getExtensionSip())
                .activo(usuario.getActivo())
                .verificadoEmail(usuario.getVerificadoEmail())
                .fechaCreacion(usuario.getFechaCreacion())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .roleNombres(roles)
                .permisos(permisos)
                .build();
    }
}
