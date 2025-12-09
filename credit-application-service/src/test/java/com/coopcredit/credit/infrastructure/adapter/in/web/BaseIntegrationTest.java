package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.TestcontainersConfiguration;
import com.coopcredit.credit.domain.model.Rol;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import com.coopcredit.credit.infrastructure.config.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Clase base para tests de integración.
 * Configura Testcontainers, MockMvc y utilidades comunes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UsuarioJpaRepository usuarioRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected UsuarioEntity adminUser;
    protected UsuarioEntity analistaUser;
    protected UsuarioEntity afiliadoUser;

    @BeforeEach
    void setUpBaseUsers() {
        // Buscar usuarios existentes o crearlos si no existen
        adminUser = usuarioRepository.findByUsername("admin")
                .orElseGet(() -> usuarioRepository.save(UsuarioEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("password123"))
                        .email("admin@test.com")
                        .roles(Set.of(Rol.ROLE_ADMIN))
                        .build()));

        analistaUser = usuarioRepository.findByUsername("analista")
                .orElseGet(() -> usuarioRepository.save(UsuarioEntity.builder()
                        .username("analista")
                        .password(passwordEncoder.encode("password123"))
                        .email("analista@test.com")
                        .roles(Set.of(Rol.ROLE_ANALISTA))
                        .build()));

        afiliadoUser = usuarioRepository.findByUsername("juanperez")
                .orElseGet(() -> usuarioRepository.save(UsuarioEntity.builder()
                        .username("juanperez")
                        .password(passwordEncoder.encode("password123"))
                        .email("juan@test.com")
                        .roles(Set.of(Rol.ROLE_AFILIADO))
                        .build()));
    }

    /**
     * Genera un token JWT para un usuario con rol ADMIN.
     */
    protected String generateAdminToken() {
        return jwtService.generateToken("admin");
    }

    /**
     * Genera un token JWT para un usuario con rol ANALISTA.
     */
    protected String generateAnalistaToken() {
        return jwtService.generateToken("analista");
    }

    /**
     * Genera un token JWT para un usuario con rol AFILIADO.
     */
    protected String generateAfiliadoToken() {
        return jwtService.generateToken("juanperez");
    }

    /**
     * Genera un token JWT para un usuario específico.
     */
    protected String generateTokenForUser(String username) {
        return jwtService.generateToken(username);
    }

    /**
     * Convierte un objeto a JSON string.
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
