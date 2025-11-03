package com.cashi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolResponse {
    private Long idRol;
    private String nombreRol;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private Set<Long> permisoIds;
    private Set<RolAsignacionResponse> asignaciones;
}
