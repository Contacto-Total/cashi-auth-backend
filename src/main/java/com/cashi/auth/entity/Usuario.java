package com.cashi.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "primer_nombre", length = 50)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "primer_apellido", length = 50)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 50)
    private String segundoApellido;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "extension_sip", length = 20)
    private String extensionSip;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "verificado_email")
    @Builder.Default
    private Boolean verificadoEmail = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "intentos_fallidos")
    @Builder.Default
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    // Relación con Roles (Many-to-Many)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_roles",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    // Relación con Tokens
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Token> tokens = new HashSet<>();

    // ===== Métodos de UserDetails =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Agregar roles
        authorities.addAll(roles.stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol()))
            .collect(Collectors.toSet()));

        // Agregar permisos de cada rol
        roles.forEach(rol -> {
            authorities.addAll(rol.getPermisos().stream()
                .map(permiso -> new SimpleGrantedAuthority(permiso.getCodigoPermiso()))
                .collect(Collectors.toSet()));
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return bloqueadoHasta == null || LocalDateTime.now().isAfter(bloqueadoHasta);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    // ===== Métodos de utilidad =====

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void agregarRol(Rol rol) {
        this.roles.add(rol);
    }

    public void removerRol(Rol rol) {
        this.roles.remove(rol);
    }

    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 5) {
            this.bloqueadoHasta = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetearIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }
}
