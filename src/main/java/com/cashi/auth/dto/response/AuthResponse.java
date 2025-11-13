package com.cashi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long idUsuario;
    private String nombreUsuario;
    private String email;
    private String nombreCompleto;
    private String extensionSip; // Extensión SIP para FreeSWITCH
    private String accessToken;
    private String refreshToken;
    private String tipoToken;
    private Long expiresIn; // en milisegundos
    private Set<String> roles;
    private Set<String> permisos;

    // Asignación del rol (tenant/cartera/subcartera)
    private Long tenantId;
    private Long portfolioId;
    private Long subPortfolioId;

    @Builder.Default
    private String mensaje = "Autenticación exitosa";
}
