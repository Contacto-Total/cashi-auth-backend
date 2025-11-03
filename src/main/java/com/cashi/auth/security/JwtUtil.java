package com.cashi.auth.security;

import com.cashi.auth.service.ConfiguracionSesionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final ConfiguracionSesionService configuracionService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long defaultExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long defaultRefreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extraer el nombre de usuario del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer la fecha de expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer un claim específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraer todos los claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generar Access Token
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // DEBUG: Ver qué authorities tiene
        System.out.println("=== DEBUG JwtUtil.generateAccessToken ===");
        System.out.println("UserDetails username: " + userDetails.getUsername());
        System.out.println("Authorities count: " + userDetails.getAuthorities().size());
        userDetails.getAuthorities().forEach(auth ->
            System.out.println("  - Authority: " + auth.getAuthority()));
        System.out.println("=== FIN DEBUG ===");

        // Agregar roles y permisos
        claims.put("roles", userDetails.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))  // Quitar prefijo "ROLE_"
                .collect(Collectors.toList()));

        claims.put("permisos", userDetails.getAuthorities().stream()
                .filter(auth -> !auth.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        System.out.println("Roles en JWT: " + claims.get("roles"));
        System.out.println("Permisos en JWT: " + claims.get("permisos"));

        // Obtener tiempo de expiración de la configuración (en segundos) y convertir a milisegundos
        Long expirationMs = getAccessTokenExpiration() * 1000;
        return createToken(claims, userDetails.getUsername(), expirationMs);
    }

    // Generar Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        // Obtener tiempo de expiración de la configuración (en segundos) y convertir a milisegundos
        Long refreshExpirationMs = getRefreshTokenExpiration() * 1000;
        return createToken(claims, userDetails.getUsername(), refreshExpirationMs);
    }

    // Obtener tiempo de expiración del access token desde configuración
    private Long getAccessTokenExpiration() {
        try {
            return configuracionService.obtenerValor(ConfiguracionSesionService.ACCESS_TOKEN_EXPIRATION).longValue();
        } catch (Exception e) {
            log.warn("No se pudo obtener configuración de expiración de access token, usando valor por defecto");
            return defaultExpiration / 1000; // Convertir ms a segundos
        }
    }

    // Obtener tiempo de expiración del refresh token desde configuración
    private Long getRefreshTokenExpiration() {
        try {
            return configuracionService.obtenerValor(ConfiguracionSesionService.REFRESH_TOKEN_EXPIRATION).longValue();
        } catch (Exception e) {
            log.warn("No se pudo obtener configuración de expiración de refresh token, usando valor por defecto");
            return defaultRefreshExpiration / 1000; // Convertir ms a segundos
        }
    }

    // Crear token
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    // Validar token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    // Validar token sin UserDetails
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    // Obtener tiempo de expiración en milisegundos
    public Long getExpirationTime() {
        return getAccessTokenExpiration() * 1000;
    }

    public Long getRefreshExpirationTime() {
        return getRefreshTokenExpiration() * 1000;
    }
}
