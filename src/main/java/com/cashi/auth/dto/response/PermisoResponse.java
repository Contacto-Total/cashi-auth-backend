package com.cashi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoResponse {
    private Long idPermiso;
    private String codigoPermiso;
    private String nombrePermiso;
    private String descripcion;
    private String categoria;
    private Boolean activo;
    private Integer ordenVisualizacion;
}
