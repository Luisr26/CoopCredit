package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.application.dto.LoginRequest;
import com.coopcredit.credit.application.dto.RegistroRequest;
import com.coopcredit.credit.domain.model.Rol;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController.
 * Verifica el flujo de registro y login de usuarios.
 */
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsuarioEntity usuarioExistente;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        usuarioRepository.deleteAll();

        // Crear usuario de prueba
        usuarioExistente = new UsuarioEntity();
        usuarioExistente.setUsername("testuser");
        usuarioExistente.setPassword(passwordEncoder.encode("password123"));
        usuarioExistente.setEmail("testuser@example.com");
        usuarioExistente.setRoles(Set.of(Rol.ROLE_AFILIADO));
        usuarioExistente = usuarioRepository.save(usuarioExistente);
    }

    @Nested
    @DisplayName("POST /auth/login - Iniciar sesión")
    class LoginTests {

        @Test
        @DisplayName("Debe retornar token JWT con credenciales válidas")
        void login_DebeRetornarTokenConCredencialesValidas() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.username", is("testuser")));
        }

        @Test
        @DisplayName("Debe retornar 401 con credenciales inválidas")
        void login_DebeRetornar401ConCredencialesInvalidas() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debe retornar 401 cuando usuario no existe")
        void login_DebeRetornar401CuandoUsuarioNoExiste() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent", "password123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando faltan campos obligatorios")
        void login_DebeRetornar400CuandoFaltanCampos() throws Exception {
            LoginRequest request = new LoginRequest("", "");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/register - Registrar usuario")
    class RegistrarTests {

        @Test
        @DisplayName("Debe registrar nuevo usuario exitosamente")
        void registrar_DebeRegistrarNuevoUsuario() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "newuser",
                    "securepass123",
                    "newuser@example.com",
                    Set.of(Rol.ROLE_AFILIADO),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.username", is("newuser")))
                    .andExpect(jsonPath("$.email", is("newuser@example.com")));
        }

        @Test
        @DisplayName("Debe retornar error cuando username ya existe")
        void registrar_DebeRetornarErrorCuandoUsernameExiste() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "testuser", // Ya existe
                    "password123",
                    "another@example.com",
                    Set.of(Rol.ROLE_AFILIADO),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debe retornar error cuando email ya existe")
        void registrar_DebeRetornarErrorCuandoEmailExiste() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "differentuser",
                    "password123",
                    "testuser@example.com", // Ya existe
                    Set.of(Rol.ROLE_AFILIADO),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando password es muy corto")
        void registrar_DebeRetornar400CuandoPasswordMuyCorto() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "validuser",
                    "123", // Muy corto
                    "valid@example.com",
                    Set.of(Rol.ROLE_AFILIADO),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando email es inválido")
        void registrar_DebeRetornar400CuandoEmailInvalido() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "validuser",
                    "validpass123",
                    "invalid-email", // Formato inválido
                    Set.of(Rol.ROLE_AFILIADO),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando username es muy corto")
        void registrar_DebeRetornar400CuandoUsernameMuyCorto() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "ab", // Muy corto
                    "validpass123",
                    "valid@example.com",
                    Set.of(Rol.ROLE_AFILIADO),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe registrar usuario con múltiples roles")
        void registrar_DebeRegistrarUsuarioConMultiplesRoles() throws Exception {
            RegistroRequest request = new RegistroRequest(
                    "adminuser",
                    "adminpass123",
                    "admin@example.com",
                    Set.of(Rol.ROLE_ADMIN, Rol.ROLE_ANALISTA),
                    null
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username", is("adminuser")));
        }
    }

    @Nested
    @DisplayName("Tests de seguridad adicionales")
    class SeguridadTests {

        @Test
        @DisplayName("El token generado debe ser válido para hacer requests autenticados")
        void tokenGenerado_DebeSerValidoParaRequests() throws Exception {
            // Primero login
            LoginRequest loginRequest = new LoginRequest("testuser", "password123");

            String responseBody = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Extraer token del response
            String token = objectMapper.readTree(responseBody).get("token").asText();

            // Verificar que el token es válido (tiene formato JWT)
            org.assertj.core.api.Assertions.assertThat(token)
                    .isNotEmpty()
                    .contains(".");
        }
    }
}
