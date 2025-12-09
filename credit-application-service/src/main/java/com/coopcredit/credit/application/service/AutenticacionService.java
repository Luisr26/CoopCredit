package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.AuthResponse;
import com.coopcredit.credit.application.dto.LoginRequest;
import com.coopcredit.credit.application.dto.RegistroRequest;
import com.coopcredit.credit.application.port.in.AutenticarUsuarioUseCase;
import com.coopcredit.credit.application.port.in.RegistrarUsuarioUseCase;
import com.coopcredit.credit.application.port.out.AfiliadoRepositoryPort;
import com.coopcredit.credit.application.port.out.UsuarioRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoNoEncontradoException;
import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.Usuario;
import com.coopcredit.credit.infrastructure.config.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación y registro de usuarios.
 */
@Service
@Transactional
public class AutenticacionService implements RegistrarUsuarioUseCase, AutenticarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(AutenticacionService.class);

    private final UsuarioRepositoryPort usuarioRepository;
    private final AfiliadoRepositoryPort afiliadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AutenticacionService(UsuarioRepositoryPort usuarioRepository,
            AfiliadoRepositoryPort afiliadoRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.afiliadoRepository = afiliadoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse registrar(RegistroRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getUsername());

        // Validar que username y email no existan
        if (usuarioRepository.existePorUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está registrado");
        }
        if (usuarioRepository.existePorEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Buscar afiliado si se proporciona ID
        Afiliado afiliado = null;
        if (request.getAfiliadoId() != null) {
            afiliado = afiliadoRepository.buscarPorId(request.getAfiliadoId())
                    .orElseThrow(() -> new AfiliadoNoEncontradoException(request.getAfiliadoId()));
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEmail(request.getEmail());
        usuario.setRoles(request.getRoles());
        usuario.setAfiliado(afiliado);

        // Guardar
        Usuario usuarioGuardado = usuarioRepository.guardar(usuario);

        // Generar token
        String token = jwtService.generateToken(usuarioGuardado.getUsername());

        log.info("Usuario registrado exitosamente: {}", usuarioGuardado.getUsername());

        return new AuthResponse(token, usuarioGuardado.getUsername(), usuarioGuardado.getEmail());
    }

    @Override
    public AuthResponse autenticar(LoginRequest request) {
        log.info("Autenticando usuario: {}", request.getUsername());

        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Buscar usuario
        Usuario usuario = usuarioRepository.buscarPorUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar token
        String token = jwtService.generateToken(usuario.getUsername());

        log.info("Usuario autenticado exitosamente: {}", usuario.getUsername());

        return new AuthResponse(token, usuario.getUsername(), usuario.getEmail());
    }
}
