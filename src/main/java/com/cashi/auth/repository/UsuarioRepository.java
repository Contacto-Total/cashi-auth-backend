package com.cashi.auth.repository;

import com.cashi.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Query con EntityGraph para forzar carga de roles y permisos
    @Query("SELECT u FROM Usuario u WHERE u.nombreUsuario = :nombreUsuario")
    @EntityGraph(attributePaths = {"roles", "roles.permisos"})
    Optional<Usuario> findByNombreUsuarioWithRoles(@Param("nombreUsuario") String nombreUsuario);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNombreUsuarioOrEmail(String nombreUsuario, String email);

    Boolean existsByNombreUsuario(String nombreUsuario);

    Boolean existsByEmail(String email);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoFalse();

    @Query("SELECT u FROM Usuario u WHERE u.bloqueadoHasta IS NOT NULL AND u.bloqueadoHasta > :now")
    List<Usuario> findUsuariosBloqueados(LocalDateTime now);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombreRol = :nombreRol")
    List<Usuario> findByRolNombre(String nombreRol);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    Long countUsuariosActivos();
}
