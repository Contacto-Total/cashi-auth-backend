package com.cashi.auth.repository;

import com.cashi.auth.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByCodigoPermiso(String codigoPermiso);

    List<Permiso> findByCategoria(String categoria);

    List<Permiso> findByActivoTrueOrderByOrdenVisualizacion();

    Boolean existsByCodigoPermiso(String codigoPermiso);
}
