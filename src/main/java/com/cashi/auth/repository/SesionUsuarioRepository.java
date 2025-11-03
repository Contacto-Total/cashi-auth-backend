package com.cashi.auth.repository;

import com.cashi.auth.entity.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    Optional<SesionUsuario> findBySessionId(String sessionId);

    List<SesionUsuario> findByUsuarioIdUsuarioAndActivaTrue(Long idUsuario);

    @Query("SELECT s FROM SesionUsuario s WHERE s.usuario.idUsuario = :idUsuario ORDER BY s.fechaInicio DESC")
    List<SesionUsuario> findByUsuarioIdUsuarioOrderByFechaInicioDesc(Long idUsuario);

    @Modifying
    @Query("UPDATE SesionUsuario s SET s.activa = false, s.fechaFin = :now WHERE s.usuario.idUsuario = :idUsuario AND s.activa = true")
    void cerrarTodasLasSesionesDelUsuario(Long idUsuario, LocalDateTime now);

    @Query("SELECT COUNT(s) FROM SesionUsuario s WHERE s.activa = true")
    Long countSesionesActivas();
}
