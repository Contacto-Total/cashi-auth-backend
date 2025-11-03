package com.cashi.auth.repository;

import com.cashi.auth.entity.ConfiguracionSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionSesionRepository extends JpaRepository<ConfiguracionSesion, Long> {

    Optional<ConfiguracionSesion> findByClave(String clave);

    Optional<ConfiguracionSesion> findByClaveAndActivoTrue(String clave);
}
