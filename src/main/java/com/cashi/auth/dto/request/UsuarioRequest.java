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
public class UsuarioRequest {

    @NotBlank(message = "El primer nombre es requerido")
    @Size(max = 50, message = "El primer nombre no puede exceder 50 caracteres")
    private String primerNombre;

    @Size(max = 50, message = "El segundo nombre no puede exceder 50 caracteres")
    private String segundoNombre;

    @NotBlank(message = "El primer apellido es requerido")
    @Size(max = 50, message = "El primer apellido no puede exceder 50 caracteres")
    private String primerApellido;

    @Size(max = 50, message = "El segundo apellido no puede exceder 50 caracteres")
    private String segundoApellido;

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
    private String nombreUsuario;

    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 20, message = "La extensión SIP no puede exceder 20 caracteres")
    private String extensionSip;

    private Boolean activo = true;

    private Set<Long> roleIds;
}
