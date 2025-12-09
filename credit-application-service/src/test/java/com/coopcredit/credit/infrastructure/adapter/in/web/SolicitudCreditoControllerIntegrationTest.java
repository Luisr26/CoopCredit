package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.application.dto.CrearSolicitudRequest;
import com.coopcredit.credit.domain.model.EstadoAfiliado;
import com.coopcredit.credit.domain.model.EstadoSolicitud;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.AfiliadoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.SolicitudCreditoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.AfiliadoJpaRepository;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.SolicitudCreditoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para SolicitudCreditoController.
 */
@DisplayName("SolicitudCreditoController Integration Tests")
class SolicitudCreditoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SolicitudCreditoJpaRepository solicitudRepository;

    @Autowired
    private AfiliadoJpaRepository afiliadoRepository;

    private AfiliadoEntity afiliadoActivo;
    private AfiliadoEntity afiliadoInactivo;
    private SolicitudCreditoEntity solicitudPendiente;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        solicitudRepository.deleteAll();
        afiliadoRepository.deleteAll();

        // Crear afiliado activo
        afiliadoActivo = new AfiliadoEntity();
        afiliadoActivo.setDocumento("123456789");
        afiliadoActivo.setNombre("Juan Activo");
        afiliadoActivo.setSalario(new BigDecimal("3000000"));
        afiliadoActivo.setFechaAfiliacion(LocalDate.now().minusMonths(12));
        afiliadoActivo.setEstado(EstadoAfiliado.ACTIVO);
        afiliadoActivo = afiliadoRepository.save(afiliadoActivo);

        // Crear afiliado inactivo
        afiliadoInactivo = new AfiliadoEntity();
        afiliadoInactivo.setDocumento("987654321");
        afiliadoInactivo.setNombre("María Inactiva");
        afiliadoInactivo.setSalario(new BigDecimal("2500000"));
        afiliadoInactivo.setFechaAfiliacion(LocalDate.now().minusMonths(24));
        afiliadoInactivo.setEstado(EstadoAfiliado.INACTIVO);
        afiliadoInactivo = afiliadoRepository.save(afiliadoInactivo);

        // Crear solicitud pendiente
        solicitudPendiente = new SolicitudCreditoEntity();
        solicitudPendiente.setAfiliado(afiliadoActivo);
        solicitudPendiente.setMonto(new BigDecimal("5000000"));
        solicitudPendiente.setPlazoMeses(24);
        solicitudPendiente.setTasaPropuesta(new BigDecimal("15.00"));
        solicitudPendiente.setFechaSolicitud(LocalDateTime.now());
        solicitudPendiente.setEstado(EstadoSolicitud.PENDIENTE);
        solicitudPendiente = solicitudRepository.save(solicitudPendiente);
    }

    @Nested
    @DisplayName("POST /api/solicitudes - Crear solicitud")
    class CrearSolicitudTests {

        @Test
        @DisplayName("Debe crear solicitud para afiliado activo")
        void crear_DebeCrearSolicitudParaAfiliadoActivo() throws Exception {
            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    afiliadoActivo.getId(),
                    new BigDecimal("8000000"),
                    36,
                    new BigDecimal("12.50")
            );

            mockMvc.perform(post("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.monto", is(8000000)))
                    .andExpect(jsonPath("$.plazoMeses", is(36)))
                    .andExpect(jsonPath("$.estado", is("PENDIENTE")));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando afiliado está inactivo")
        void crear_DebeRetornar400CuandoAfiliadoInactivo() throws Exception {
            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    afiliadoInactivo.getId(),
                    new BigDecimal("5000000"),
                    24,
                    new BigDecimal("15.00")
            );

            mockMvc.perform(post("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando afiliado no existe")
        void crear_DebeRetornar404CuandoAfiliadoNoExiste() throws Exception {
            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    99999L,
                    new BigDecimal("5000000"),
                    24,
                    new BigDecimal("15.00")
            );

            mockMvc.perform(post("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Debe retornar 400 cuando datos son inválidos")
        void crear_DebeRetornar400CuandoDatosInvalidos() throws Exception {
            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    afiliadoActivo.getId(),
                    new BigDecimal("-1000"), // Monto negativo
                    0, // Plazo inválido
                    new BigDecimal("150.00") // Tasa excede límite
            );

            mockMvc.perform(post("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAdminToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe retornar 401 sin autenticación")
        void crear_DebeRetornar401SinAutenticacion() throws Exception {
            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    afiliadoActivo.getId(),
                    new BigDecimal("5000000"),
                    24,
                    new BigDecimal("15.00")
            );

            mockMvc.perform(post("/api/solicitudes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isForbidden()); // Spring Security devuelve 403 sin autenticación
        }
    }

    @Nested
    @DisplayName("GET /api/solicitudes/{id} - Obtener solicitud")
    class ObtenerSolicitudTests {

        @Test
        @DisplayName("Debe retornar solicitud cuando existe")
        void obtenerPorId_DebeRetornarSolicitudCuandoExiste() throws Exception {
            mockMvc.perform(get("/api/solicitudes/" + solicitudPendiente.getId())
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(solicitudPendiente.getId().intValue())))
                    .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                    .andExpect(jsonPath("$.monto", is(5000000)));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando solicitud no existe")
        void obtenerPorId_DebeRetornar404CuandoNoExiste() throws Exception {
            mockMvc.perform(get("/api/solicitudes/99999")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/solicitudes - Listar solicitudes")
    class ListarSolicitudesTests {

        @Test
        @DisplayName("Debe retornar lista de solicitudes para ADMIN")
        void listarTodas_DebeRetornarListaParaAdmin() throws Exception {
            mockMvc.perform(get("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Debe retornar lista de solicitudes para ANALISTA")
        void listarTodas_DebeRetornarListaParaAnalista() throws Exception {
            mockMvc.perform(get("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAnalistaToken()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debe retornar 403 para AFILIADO")
        void listarTodas_DebeRetornar403ParaAfiliado() throws Exception {
            mockMvc.perform(get("/api/solicitudes")
                            .header("Authorization", "Bearer " + generateAfiliadoToken()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/solicitudes/afiliado/{afiliadoId} - Listar por afiliado")
    class ListarPorAfiliadoTests {

        @Test
        @DisplayName("Debe retornar solicitudes del afiliado")
        void listarPorAfiliado_DebeRetornarSolicitudesDelAfiliado() throws Exception {
            mockMvc.perform(get("/api/solicitudes/afiliado/" + afiliadoActivo.getId())
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando afiliado no tiene solicitudes")
        void listarPorAfiliado_DebeRetornarListaVacia() throws Exception {
            mockMvc.perform(get("/api/solicitudes/afiliado/" + afiliadoInactivo.getId())
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/solicitudes/estado/{estado} - Listar por estado")
    class ListarPorEstadoTests {

        @Test
        @DisplayName("Debe retornar solicitudes pendientes")
        void listarPorEstado_DebeRetornarSolicitudesPendientes() throws Exception {
            mockMvc.perform(get("/api/solicitudes/estado/PENDIENTE")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].estado", is("PENDIENTE")));
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay solicitudes rechazadas")
        void listarPorEstado_DebeRetornarListaVaciaSinRechazadas() throws Exception {
            mockMvc.perform(get("/api/solicitudes/estado/RECHAZADO")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("POST /api/solicitudes/{id}/evaluar - Evaluar solicitud")
    class EvaluarSolicitudTests {

        @Test
        @DisplayName("Debe retornar 403 para AFILIADO al evaluar")
        void evaluar_DebeRetornar403ParaAfiliado() throws Exception {
            mockMvc.perform(post("/api/solicitudes/" + solicitudPendiente.getId() + "/evaluar")
                            .header("Authorization", "Bearer " + generateAfiliadoToken()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debe retornar 404 cuando solicitud no existe")
        void evaluar_DebeRetornar404CuandoSolicitudNoExiste() throws Exception {
            mockMvc.perform(post("/api/solicitudes/99999/evaluar")
                            .header("Authorization", "Bearer " + generateAdminToken()))
                    .andExpect(status().isNotFound());
        }
    }
}
