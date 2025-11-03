package com.cashi.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "roles_asignaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Long idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "tipo_asignacion", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoAsignacion tipoAsignacion; // INQUILINO, CARTERA, SUBCARTERA

    @Column(name = "id_inquilino", nullable = false)
    private Long tenantId;

    @Column(name = "id_cartera")
    private Long portfolioId;

    @Column(name = "id_subcartera")
    private Long subPortfolioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum TipoAsignacion {
        INQUILINO,
        CARTERA,
        SUBCARTERA
    }
}
