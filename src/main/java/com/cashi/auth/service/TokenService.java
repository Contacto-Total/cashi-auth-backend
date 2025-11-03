package com.cashi.auth.service;

import com.cashi.auth.entity.Token;
import com.cashi.auth.entity.Usuario;
import com.cashi.auth.repository.TokenRepository;
import com.cashi.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public Token guardarToken(String tokenStr, Usuario usuario, Token.TipoToken tipoToken, String ipCliente, String userAgent, String dispositivo) {
        LocalDateTime expiracion = tipoToken == Token.TipoToken.ACCESS
                ? LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime() / 1000)
                : LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationTime() / 1000);

        Token token = Token.builder()
                .token(tokenStr)
                .tipoToken(tipoToken)
                .usuario(usuario)
                .fechaExpiracion(expiracion)
                .ipCliente(ipCliente)
                .userAgent(userAgent)
                .dispositivo(dispositivo)
                .activo(true)
                .revocado(false)
                .build();

        return tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public boolean esTokenValido(String tokenStr) {
        return tokenRepository.findByToken(tokenStr)
                .map(Token::esValido)
                .orElse(false);
    }

    @Transactional
    public void revocarToken(String tokenStr) {
        tokenRepository.findByToken(tokenStr).ifPresent(token -> {
            token.revocar();
            tokenRepository.save(token);
            log.info("Token revocado: {}", tokenStr.substring(0, 20) + "...");
        });
    }

    @Transactional
    public void revocarTodosLosTokensDelUsuario(Long idUsuario) {
        tokenRepository.revocarTodosLosTokensDelUsuario(idUsuario, LocalDateTime.now());
        log.info("Todos los tokens del usuario {} han sido revocados", idUsuario);
    }

    @Transactional(readOnly = true)
    public List<Token> obtenerTokensActivosDelUsuario(Long idUsuario) {
        return tokenRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .filter(Token::esValido)
                .toList();
    }

    // Tarea programada para limpiar tokens expirados
    @Scheduled(cron = "${token.cleanup.cron}")
    @Transactional
    public void limpiarTokensExpirados() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30); // Eliminar tokens con más de 30 días
        int tokensEliminados = tokenRepository.findTokensExpirados(LocalDateTime.now()).size();
        tokenRepository.eliminarTokensExpirados(fechaLimite);
        log.info("Limpieza de tokens completada. {} tokens eliminados", tokensEliminados);
    }
}
