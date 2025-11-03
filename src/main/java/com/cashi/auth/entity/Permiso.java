package com.cashi.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permisos", uniqueConstraints = {
    @UniqueConstraint(columnNames = "codigo_permiso")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @Column(name = "codigo_permiso", nullable = false, unique = true, length = 100)
    private String codigoPermiso;

    @Column(name = "nombre_permiso", nullable = false, length = 150)
    private String nombrePermiso;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria; // CLIENTES, GESTIONES, PAGOS, REPORTES, CONFIGURACION, etc.

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "orden_visualizacion")
    private Integer ordenVisualizacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Relación inversa con Roles
    @ManyToMany(mappedBy = "permisos")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Rol> roles = new HashSet<>();

    public Permiso(String codigoPermiso, String nombrePermiso, String descripcion,
                   String categoria, Integer ordenVisualizacion) {
        this.codigoPermiso = codigoPermiso;
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ordenVisualizacion = ordenVisualizacion;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
}
