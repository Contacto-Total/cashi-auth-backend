package com.cashi.auth.service;

import com.cashi.auth.dto.request.UsuarioRequest;
import com.cashi.auth.dto.response.UsuarioResponse;
import com.cashi.auth.entity.Rol;
import com.cashi.auth.entity.Usuario;
import com.cashi.auth.repository.RolRepository;
import com.cashi.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioManagementService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final int PASSWORD_LENGTH = 12;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return convertirAResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        // Validar que el nombre de usuario no exista
        if (usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // Generar contraseña automática
        String passwordGenerada = generarPasswordAleatoria();

        // Construir nombre completo
        String nombreCompleto = construirNombreCompleto(
                request.getPrimerNombre(),
                request.getSegundoNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido()
        );

        // Crear entidad Usuario
        Usuario usuario = Usuario.builder()
                .primerNombre(request.getPrimerNombre())
                .segundoNombre(request.getSegundoNombre())
                .primerApellido(request.getPrimerApellido())
                .segundoApellido(request.getSegundoApellido())
                .nombreCompleto(nombreCompleto)
                .nombreUsuario(request.getNombreUsuario())
                .email(request.getEmail() != null ? request.getEmail() : request.getNombreUsuario() + "@cashi.com")
                .contrasena(passwordEncoder.encode(passwordGenerada))
                .telefono(request.getTelefono())
                .extensionSip(request.getExtensionSip())
                .activo(request.getActivo())
                .build();

        // Asignar roles
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Rol> roles = new HashSet<>(rolRepository.findAllById(request.getRoleIds()));
            usuario.setRoles(roles);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        UsuarioResponse response = convertirAResponse(usuarioGuardado);
        // Agregar la contraseña generada al response solo en creación
        response.setGeneratedPassword(passwordGenerada);

        return response;
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Validar que el nombre de usuario no exista en otro usuario
        if (request.getNombreUsuario() != null && !request.getNombreUsuario().equals(usuario.getNombreUsuario())) {
            if (usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).isPresent()) {
                throw new RuntimeException("El nombre de usuario ya existe");
            }
            usuario.setNombreUsuario(request.getNombreUsuario());
        }

        // Actualizar campos
        usuario.setPrimerNombre(request.getPrimerNombre());
        usuario.setSegundoNombre(request.getSegundoNombre());
        usuario.setPrimerApellido(request.getPrimerApellido());
        usuario.setSegundoApellido(request.getSegundoApellido());
        usuario.setNombreCompleto(construirNombreCompleto(
                request.getPrimerNombre(),
                request.getSegundoNombre(),
                request.getPrimerApellido(),
                request.getSegundoApellido()
        ));

        if (request.getEmail() != null) {
            usuario.setEmail(request.getEmail());
        }

        usuario.setTelefono(request.getTelefono());
        usuario.setExtensionSip(request.getExtensionSip());
        usuario.setActivo(request.getActivo());

        // Actualizar roles
        if (request.getRoleIds() != null) {
            Set<Rol> roles = new HashSet<>(rolRepository.findAllById(request.getRoleIds()));
            usuario.setRoles(roles);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioActualizado);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        Set<Long> roleIds = usuario.getRoles().stream()
                .map(Rol::getIdRol)
                .collect(Collectors.toSet());

        Set<String> roleNombres = usuario.getRoles().stream()
                .map(Rol::getNombreRol)
                .collect(Collectors.toSet());

        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .primerNombre(usuario.getPrimerNombre())
                .segundoNombre(usuario.getSegundoNombre())
                .primerApellido(usuario.getPrimerApellido())
                .segundoApellido(usuario.getSegundoApellido())
                .nombreCompleto(usuario.getNombreCompleto())
                .nombreUsuario(usuario.getNombreUsuario())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .extensionSip(usuario.getExtensionSip())
                .activo(usuario.getActivo())
                .verificadoEmail(usuario.getVerificadoEmail())
                .fechaCreacion(usuario.getFechaCreacion())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .roleIds(roleIds)
                .roleNombres(roleNombres)
                .build();
    }

    private String construirNombreCompleto(String primerNombre, String segundoNombre,
                                           String primerApellido, String segundoApellido) {
        StringBuilder nombreCompleto = new StringBuilder();

        if (primerNombre != null && !primerNombre.trim().isEmpty()) {
            nombreCompleto.append(primerNombre.trim());
        }

        if (segundoNombre != null && !segundoNombre.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(segundoNombre.trim());
        }

        if (primerApellido != null && !primerApellido.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(primerApellido.trim());
        }

        if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(segundoApellido.trim());
        }

        return nombreCompleto.toString();
    }

    private String generarPasswordAleatoria() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(randomIndex));
        }

        return password.toString();
    }
}
