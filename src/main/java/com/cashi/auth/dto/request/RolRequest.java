package com.cashi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolRequest {

    @NotBlank(message = "El nombre del rol es requerido")
    @Size(max = 50, message = "El nombre del rol no puede exceder 50 caracteres")
    private String nombreRol;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    private Boolean activo = true;

    private Set<Long> permisoIds;

    private Set<RolAsignacionRequest> asignaciones;
}
