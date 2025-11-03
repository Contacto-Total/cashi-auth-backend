package com.cashi.auth.entity;

import jakarta.persistence.*;
import lombok.*;



import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol", unique = true, nullable = false, length = 50)
    private String nombreRol; // ADMIN, SUPERVISOR, AGENTE, etc.

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Relación con Permisos (Many-to-Many)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "roles_permisos",
        joinColumns = @JoinColumn(name = "id_rol"),
        inverseJoinColumns = @JoinColumn(name = "id_permiso")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();

    // Relación inversa con Usuarios
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Usuario> usuarios = new HashSet<>();

    // Relación con Asignaciones
    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<RolAsignacion> asignaciones = new HashSet<>();

    public void agregarPermiso(Permiso permiso) {
        this.permisos.add(permiso);
    }

    public void removerPermiso(Permiso permiso) {
        this.permisos.remove(permiso);
    }

    public void agregarAsignacion(RolAsignacion asignacion) {
        this.asignaciones.add(asignacion);
        asignacion.setRol(this);
    }

    public void removerAsignacion(RolAsignacion asignacion) {
        this.asignaciones.remove(asignacion);
        asignacion.setRol(null);
    }
}
