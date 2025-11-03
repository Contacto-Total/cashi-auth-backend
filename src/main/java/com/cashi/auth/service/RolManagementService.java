package com.cashi.auth.service;

import com.cashi.auth.dto.request.RolAsignacionRequest;
import com.cashi.auth.dto.request.RolRequest;
import com.cashi.auth.dto.response.RolAsignacionResponse;
import com.cashi.auth.dto.response.RolResponse;
import com.cashi.auth.entity.Permiso;
import com.cashi.auth.entity.Rol;
import com.cashi.auth.entity.RolAsignacion;
import com.cashi.auth.repository.PermisoRepository;
import com.cashi.auth.repository.RolAsignacionRepository;
import com.cashi.auth.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolManagementService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolAsignacionRepository rolAsignacionRepository;

    @Transactional(readOnly = true)
    public List<RolResponse> obtenerTodosRoles() {
        return rolRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RolResponse obtenerRolPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        return convertirAResponse(rol);
    }

    @Transactional
    public RolResponse crearRol(RolRequest request) {
        // Validar que el nombre del rol no exista
        if (rolRepository.findByNombreRol(request.getNombreRol()).isPresent()) {
            throw new RuntimeException("El nombre del rol ya existe");
        }

        // Crear entidad Rol
        Rol rol = Rol.builder()
                .nombreRol(request.getNombreRol())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo())
                .build();

        // Asignar permisos
        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            Set<Permiso> permisos = new HashSet<>(permisoRepository.findAllById(request.getPermisoIds()));
            rol.setPermisos(permisos);
        }

        Rol rolGuardado = rolRepository.save(rol);

        // Crear asignaciones
        if (request.getAsignaciones() != null && !request.getAsignaciones().isEmpty()) {
            for (RolAsignacionRequest asignacionRequest : request.getAsignaciones()) {
                RolAsignacion asignacion = RolAsignacion.builder()
                        .rol(rolGuardado)
                        .tipoAsignacion(RolAsignacion.TipoAsignacion.valueOf(asignacionRequest.getTipoAsignacion()))
                        .tenantId(asignacionRequest.getTenantId())
                        .portfolioId(asignacionRequest.getPortfolioId())
                        .subPortfolioId(asignacionRequest.getSubPortfolioId())
                        .build();
                rolAsignacionRepository.save(asignacion);
            }
        }

        return obtenerRolPorId(rolGuardado.getIdRol());
    }

    @Transactional
    public RolResponse actualizarRol(Long id, RolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        // Validar que el nombre del rol no exista en otro rol
        if (request.getNombreRol() != null && !request.getNombreRol().equalsIgnoreCase(rol.getNombreRol())) {
            if (rolRepository.findByNombreRol(request.getNombreRol()).isPresent()) {
                throw new RuntimeException("El nombre del rol ya existe");
            }
            rol.setNombreRol(request.getNombreRol());
        } else if (request.getNombreRol() != null && request.getNombreRol().equalsIgnoreCase(rol.getNombreRol())) {
            // Si solo cambió mayúsculas/minúsculas, permitir la actualización
            rol.setNombreRol(request.getNombreRol());
        }

        // Actualizar campos
        rol.setDescripcion(request.getDescripcion());
        rol.setActivo(request.getActivo());

        // Actualizar permisos
        if (request.getPermisoIds() != null) {
            Set<Permiso> permisos = new HashSet<>(permisoRepository.findAllById(request.getPermisoIds()));
            rol.setPermisos(permisos);
        }

        rolRepository.save(rol);

        // Actualizar asignaciones (eliminar las existentes y crear nuevas)
        rolAsignacionRepository.deleteByRolIdRol(id);

        if (request.getAsignaciones() != null && !request.getAsignaciones().isEmpty()) {
            for (RolAsignacionRequest asignacionRequest : request.getAsignaciones()) {
                RolAsignacion asignacion = RolAsignacion.builder()
                        .rol(rol)
                        .tipoAsignacion(RolAsignacion.TipoAsignacion.valueOf(asignacionRequest.getTipoAsignacion()))
                        .tenantId(asignacionRequest.getTenantId())
                        .portfolioId(asignacionRequest.getPortfolioId())
                        .subPortfolioId(asignacionRequest.getSubPortfolioId())
                        .build();
                rolAsignacionRepository.save(asignacion);
            }
        }

        return obtenerRolPorId(id);
    }

    @Transactional
    public void eliminarRol(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new RuntimeException("Rol no encontrado con ID: " + id);
        }
        rolRepository.deleteById(id);
    }

    private RolResponse convertirAResponse(Rol rol) {
        Set<Long> permisoIds = rol.getPermisos().stream()
                .map(Permiso::getIdPermiso)
                .collect(Collectors.toSet());

        List<RolAsignacion> asignaciones = rolAsignacionRepository.findByRolIdRol(rol.getIdRol());
        Set<RolAsignacionResponse> asignacionesResponse = asignaciones.stream()
                .map(a -> RolAsignacionResponse.builder()
                        .idAsignacion(a.getIdAsignacion())
                        .tipoAsignacion(a.getTipoAsignacion().name())
                        .tenantId(a.getTenantId())
                        .portfolioId(a.getPortfolioId())
                        .subPortfolioId(a.getSubPortfolioId())
                        .build())
                .collect(Collectors.toSet());

        return RolResponse.builder()
                .idRol(rol.getIdRol())
                .nombreRol(rol.getNombreRol())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .fechaCreacion(rol.getFechaCreacion())
                .permisoIds(permisoIds)
                .asignaciones(asignacionesResponse)
                .build();
    }
}
