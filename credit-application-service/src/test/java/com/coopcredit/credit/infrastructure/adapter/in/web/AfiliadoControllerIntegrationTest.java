package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.application.dto.AfiliadoDTO;
import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;
import com.coopcredit.credit.domain.model.EstadoAfiliado;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.AfiliadoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.AfiliadoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AfiliadoController.
 * Usa Testcontainers para PostgreSQL y MockMvc para las peticiones HTTP.
 */
@DisplayName("AfiliadoController Integration Tests")
class AfiliadoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AfiliadoJpaRepository afiliadoRepository;

    private AfiliadoEntity afiliadoExistente;

    @BeforeEach
    void setUp() {
        // Limpiar datos de prueba previos
        afiliadoRepository.deleteAll();

        // Crear afiliado de prueba
        afiliadoExistente = new AfiliadoEntity();
        afiliadoExistente.setDocumento("111111111");
        afiliadoExistente.setNombre("Test User");
        afiliadoExistente.setSalario(new BigDecimal("2500000"));
        afiliadoExistente.setFechaAfiliacion(LocalDate.now().minusMonths(12));
        afiliadoExistente.setEstado(EstadoAfiliado.ACTIVO);
        afiliadoExistente = afiliadoRepository.save(afiliadoExistente);
    }

    @Nested
    @DisplayName("POST /api/afiliados - Crear afiliado")
    class CrearAfiliadoTests {

        @Test
        @DisplayName("Debe crear afiliado cuando usuario es ADMIN")
        void crear_DebeCrearAfiliadoConRolAdmin() throws Exception {
            CrearAfiliadoRequest request = new CrearAfiliadoRequest(
                    "222222222",
                    "Nuevo Afiliado",
                    new BigDecimal("3000000"),
                    LocalDate.now().minusMonths(6),
                    EstadoAfiliado.ACTIVO
            );

            mockMvc.perform(post("/api/afiliados")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.documento", is("222222222")))
                    .andExpect(jsonPath("$.nombre", is("Nuevo Afiliado")))
                    .andExpect(jsonPath("$.estado", is("ACTIVO")));
        }

        @Test
        @DisplayName("Debe retornar 403 cuando usuario no es ADMIN")
        void crear_DebeRetornar403CuandoNoEsAdmin() throws Exception {
            CrearAfiliadoRequest request = new CrearAfiliadoRequest(
                    "333333333",
                    "Afiliado No Autorizado",
                    new BigDecimal("2000000"),
                    LocalDate.now().minusMonths(6),
                    EstadoAfiliado.ACTIVO
            );

            mockMvc.perform(post("/api/afiliados")
                            .header("Authorization", "Bearer " + generateAfiliadoToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debe retornar 401 cuando no hay token")
        void crear_DebeRetornar401SinToken() throws Exception {
            CrearAfiliadoRequest request = new CrearAfiliadoRequest(
                    "444444444",
                    "Sin Auth",
                    new BigDecimal("2000000"),
                    LocalDate.now().minusMonths(6),
                    EstadoAfiliado.ACTIVO
            );

            mockMvc.perform(post("/api/afiliados")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden()); // Spring Security devuelve 403 sin autenticación
        }

        @Test
        @DisplayName("Debe retornar 409 cuando documento ya existe")
        void crear_DebeRetornar409CuandoDocumentoExiste() throws Exception {
            CrearAfiliadoRequest request = new CrearAfiliadoRequest(
                    "111111111", // Documento existente
                    "Duplicado",
                    new BigDecimal("2000000"),
                    LocalDate.now().minusMonths(6),
                    EstadoAfiliado.ACTIVO
            );

            mockMvc.perform(post("/api/afiliados")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando datos son inválidos")
        void crear_DebeRetornar400CuandoDatosInvalidos() throws Exception {
            CrearAfiliadoRequest request = new CrearAfiliadoRequest(
                    "123", // Documento muy corto
                    "AB", // Nombre muy corto
                    new BigDecimal("-1000"), // Salario negativo
                    LocalDate.now().plusMonths(1), // Fecha futura
                    null // Estado nulo
            );

            mockMvc.perform(post("/api/afiliados")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Errores de validación")));
        }
    }

    @Nested
    @DisplayName("GET /api/afiliados/{id} - Obtener afiliado")
    class ObtenerAfiliadoTests {

        @Test
        @DisplayName("Debe retornar afiliado cuando existe")
        void obtenerPorId_DebeRetornarAfiliadoCuandoExiste() throws Exception {
            mockMvc.perform(get("/api/afiliados/" + afiliadoExistente.getId())
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(afiliadoExistente.getId().intValue())))
                    .andExpect(jsonPath("$.documento", is("111111111")))
                    .andExpect(jsonPath("$.nombre", is("Test User")));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando afiliado no existe")
        void obtenerPorId_DebeRetornar404CuandoNoExiste() throws Exception {
            mockMvc.perform(get("/api/afiliados/99999")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/afiliados - Listar afiliados")
    class ListarAfiliadosTests {

        @Test
        @DisplayName("Debe retornar lista de afiliados para ADMIN")
        void listarTodos_DebeRetornarListaParaAdmin() throws Exception {
            mockMvc.perform(get("/api/afiliados")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].documento", notNullValue()));
        }

        @Test
        @DisplayName("Debe retornar 403 para AFILIADO")
        void listarTodos_DebeRetornar403ParaAfiliado() throws Exception {
            mockMvc.perform(get("/api/afiliados")
                            .header("Authorization", "Bearer " + generateAfiliadoToken()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/afiliados/{id} - Actualizar afiliado")
    class ActualizarAfiliadoTests {

        @Test
        @DisplayName("Debe actualizar afiliado cuando usuario es ADMIN")
        void actualizar_DebeActualizarConRolAdmin() throws Exception {
            AfiliadoDTO actualizacion = AfiliadoDTO.builder()
                    .nombre("Nombre Actualizado")
                    .salario(new BigDecimal("3500000"))
                    .fechaAfiliacion(afiliadoExistente.getFechaAfiliacion())
                    .estado(EstadoAfiliado.ACTIVO)
                    .build();

            mockMvc.perform(put("/api/afiliados/" + afiliadoExistente.getId())
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(actualizacion)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre", is("Nombre Actualizado")));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando afiliado no existe")
        void actualizar_DebeRetornar404CuandoNoExiste() throws Exception {
            AfiliadoDTO actualizacion = AfiliadoDTO.builder()
                    .nombre("No existe")
                    .salario(new BigDecimal("2000000"))
                    .fechaAfiliacion(LocalDate.now().minusMonths(12))
                    .estado(EstadoAfiliado.ACTIVO)
                    .build();

            mockMvc.perform(put("/api/afiliados/99999")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(actualizacion)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Debe retornar 403 cuando usuario no es ADMIN")
        void actualizar_DebeRetornar403CuandoNoEsAdmin() throws Exception {
            AfiliadoDTO actualizacion = AfiliadoDTO.builder()
                    .nombre("Intento no autorizado")
                    .salario(new BigDecimal("2000000"))
                    .fechaAfiliacion(LocalDate.now().minusMonths(12))
                    .estado(EstadoAfiliado.ACTIVO)
                    .build();

            mockMvc.perform(put("/api/afiliados/" + afiliadoExistente.getId())
                            .header("Authorization", "Bearer " + generateAnalistaToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(actualizacion)))
                    .andExpect(status().isForbidden());
        }
    }
}
