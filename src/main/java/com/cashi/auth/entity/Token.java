package com.cashi.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_usuario_activo", columnList = "id_usuario, activo"),
    @Index(name = "idx_fecha_expiracion", columnList = "fecha_expiracion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Long idToken;

    @Column(name = "token", unique = true, nullable = false, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_token", nullable = false, length = 20)
    private TipoToken tipoToken; // ACCESS, REFRESH

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "revocado")
    @Builder.Default
    private Boolean revocado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "dispositivo", length = 100)
    private String dispositivo;

    @Column(name = "fecha_revocacion")
    private LocalDateTime fechaRevocacion;

    // Relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public enum TipoToken {
        ACCESS,
        REFRESH
    }

    public boolean estaExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    public boolean esValido() {
        return activo && !revocado && !estaExpirado();
    }

    public void revocar() {
        this.revocado = true;
        this.activo = false;
        this.fechaRevocacion = LocalDateTime.now();
    }
}
