package com.cashi.auth.repository;

import com.cashi.auth.entity.Token;
import com.cashi.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    List<Token> findByUsuarioAndActivoTrue(Usuario usuario);

    List<Token> findByUsuarioIdUsuario(Long idUsuario);

    @Query("SELECT t FROM Token t WHERE t.usuario.idUsuario = :idUsuario AND t.tipoToken = :tipoToken AND t.activo = true AND t.revocado = false")
    List<Token> findTokensActivosPorUsuarioYTipo(Long idUsuario, Token.TipoToken tipoToken);

    @Query("SELECT t FROM Token t WHERE t.fechaExpiracion < :now AND t.activo = true")
    List<Token> findTokensExpirados(LocalDateTime now);

    @Modifying
    @Query("UPDATE Token t SET t.activo = false, t.revocado = true, t.fechaRevocacion = :now WHERE t.usuario.idUsuario = :idUsuario")
    void revocarTodosLosTokensDelUsuario(Long idUsuario, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.fechaExpiracion < :fechaLimite")
    void eliminarTokensExpirados(LocalDateTime fechaLimite);
}
