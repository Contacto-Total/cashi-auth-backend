package com.cashi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSesionDTO {

    private String clave;
    private Integer valor; // En segundos
    private String descripcion;
}
