package com.cashi.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolAsignacionResponse {
    private Long idAsignacion;
    private String tipoAsignacion; // TENANT, PORTFOLIO, SUBPORTFOLIO
    private Long tenantId;
    private Long portfolioId;
    private Long subPortfolioId;
}
