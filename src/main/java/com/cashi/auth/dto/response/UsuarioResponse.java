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
public class UsuarioResponse {

    private Long idUsuario;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;
    private String nombreUsuario;
    private String email;
    private String telefono;
    private String extensionSip;
    private Boolean activo;
    private Boolean verificadoEmail;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    private Set<Long> roleIds;
    private Set<String> roleNombres;
    private Set<String> permisos; // Permisos del usuario (para autenticación)
    private String generatedPassword; // Solo se devuelve al crear el usuario
}
