package com.cashi.auth.service;

import com.cashi.auth.dto.response.PermisoResponse;
import com.cashi.auth.entity.Permiso;
import com.cashi.auth.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    @Transactional(readOnly = true)
    public List<PermisoResponse> obtenerTodosPermisos() {
        return permisoRepository.findByActivoTrueOrderByOrdenVisualizacion().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, List<PermisoResponse>> obtenerPermisosAgrupadosPorCategoria() {
        List<Permiso> permisos = permisoRepository.findByActivoTrueOrderByOrdenVisualizacion();

        // Agrupar por categoría manteniendo el orden
        Map<String, List<PermisoResponse>> permisosPorCategoria = new LinkedHashMap<>();

        permisos.stream()
                .collect(Collectors.groupingBy(
                        Permiso::getCategoria,
                        LinkedHashMap::new,
                        Collectors.mapping(this::convertirAResponse, Collectors.toList())
                ))
                .forEach(permisosPorCategoria::put);

        return permisosPorCategoria;
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> obtenerPermisosPorCategoria(String categoria) {
        return permisoRepository.findByCategoria(categoria).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    private PermisoResponse convertirAResponse(Permiso permiso) {
        return PermisoResponse.builder()
                .idPermiso(permiso.getIdPermiso())
                .codigoPermiso(permiso.getCodigoPermiso())
                .nombrePermiso(permiso.getNombrePermiso())
                .descripcion(permiso.getDescripcion())
                .categoria(permiso.getCategoria())
                .activo(permiso.getActivo())
                .ordenVisualizacion(permiso.getOrdenVisualizacion())
                .build();
    }
}
