package com.cashi.auth.repository;

import com.cashi.auth.entity.RolAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolAsignacionRepository extends JpaRepository<RolAsignacion, Long> {

    List<RolAsignacion> findByRolIdRol(Long idRol);

    void deleteByRolIdRol(Long idRol);
}
