package com.cashi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolAsignacionRequest {

    @NotBlank(message = "El tipo de asignación es requerido")
    private String tipoAsignacion; // TENANT, PORTFOLIO, SUBPORTFOLIO

    @NotNull(message = "El tenant ID es requerido")
    private Long tenantId;

    private Long portfolioId;

    private Long subPortfolioId;
}
