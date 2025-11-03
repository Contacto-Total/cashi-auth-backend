package com.cashi.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_usuario", indexes = {
    @Index(name = "idx_usuario_activa", columnList = "id_usuario, activa"),
    @Index(name = "idx_session_id", columnList = "session_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Long idSesion;

    @Column(name = "session_id", unique = true, nullable = false, length = 255)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "dispositivo", length = 100)
    private String dispositivo;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "fecha_inicio", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_ultimo_acceso")
    @Builder.Default
    private LocalDateTime fechaUltimoAcceso = LocalDateTime.now();

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    public void cerrarSesion() {
        this.activa = false;
        this.fechaFin = LocalDateTime.now();
    }

    public void actualizarUltimoAcceso() {
        this.fechaUltimoAcceso = LocalDateTime.now();
    }
}
