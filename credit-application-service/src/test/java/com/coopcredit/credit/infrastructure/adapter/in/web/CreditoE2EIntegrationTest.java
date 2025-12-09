package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;
import com.coopcredit.credit.application.dto.CrearSolicitudRequest;
import com.coopcredit.credit.domain.model.EstadoAfiliado;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.AfiliadoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.AfiliadoJpaRepository;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.SolicitudCreditoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests E2E (End-to-End) para el flujo completo de solicitud de crédito.
 * Simula el ciclo de vida completo: registro → afiliado → solicitud → evaluación
 */
@DisplayName("Crédito E2E Integration Tests")
class CreditoE2EIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AfiliadoJpaRepository afiliadoRepository;

    @Autowired
    private SolicitudCreditoJpaRepository solicitudRepository;

    @BeforeEach
    void setUp() {
        // Limpiar datos de solicitudes y afiliados (usuarios se crean en BaseIntegrationTest)
        solicitudRepository.deleteAll();
        afiliadoRepository.deleteAll();
        // Los usuarios admin, analista, juanperez se crean en BaseIntegrationTest.setUpBaseUsers()
    }

    @Test
    @DisplayName("Flujo completo: Crear afiliado → Crear solicitud → Listar solicitudes pendientes")
    void flujoCompleto_CrearAfiliadoYSolicitud() throws Exception {
        String adminToken = generateTokenForUser("admin");

        // 1. ADMIN crea un afiliado
        CrearAfiliadoRequest crearAfiliado = new CrearAfiliadoRequest(
                "123456789",
                "Juan Pérez",
                new BigDecimal("4000000"),
                LocalDate.now().minusMonths(12), // 12 meses de antigüedad
                EstadoAfiliado.ACTIVO
        );

        String afiliadoResponse = mockMvc.perform(post("/api/afiliados")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(crearAfiliado)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento", is("123456789")))
                .andExpect(jsonPath("$.puedeRecibirCredito", is(true)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long afiliadoId = objectMapper.readTree(afiliadoResponse).get("id").asLong();

        // 2. Crear solicitud de crédito para el afiliado
        CrearSolicitudRequest crearSolicitud = new CrearSolicitudRequest(
                afiliadoId,
                new BigDecimal("10000000"), // Monto dentro del límite (4M * 5 = 20M)
                36,
                new BigDecimal("12.00")
        );

        String solicitudResponse = mockMvc.perform(post("/api/solicitudes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(crearSolicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.monto", is(10000000)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long solicitudId = objectMapper.readTree(solicitudResponse).get("id").asLong();

        // 3. Verificar que la solicitud aparece en la lista de pendientes
        mockMvc.perform(get("/api/solicitudes/estado/PENDIENTE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.id == " + solicitudId + ")]", hasSize(1)));

        // 4. Verificar que se puede obtener la solicitud por ID
        mockMvc.perform(get("/api/solicitudes/" + solicitudId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitudId.intValue())))
                .andExpect(jsonPath("$.afiliadoId", is(afiliadoId.intValue())));
    }

    @Test
    @DisplayName("Flujo de rechazo: Afiliado inactivo no puede crear solicitud")
    void flujoRechazo_AfiliadoInactivoNoPuedeCrearSolicitud() throws Exception {
        String adminToken = generateTokenForUser("admin");

        // 1. Crear afiliado INACTIVO
        AfiliadoEntity afiliadoInactivo = new AfiliadoEntity();
        afiliadoInactivo.setDocumento("999888777");
        afiliadoInactivo.setNombre("Afiliado Inactivo");
        afiliadoInactivo.setSalario(new BigDecimal("3000000"));
        afiliadoInactivo.setFechaAfiliacion(LocalDate.now().minusMonths(24));
        afiliadoInactivo.setEstado(EstadoAfiliado.INACTIVO);
        afiliadoInactivo = afiliadoRepository.save(afiliadoInactivo);

        // 2. Intentar crear solicitud para afiliado inactivo
        CrearSolicitudRequest crearSolicitud = new CrearSolicitudRequest(
                afiliadoInactivo.getId(),
                new BigDecimal("5000000"),
                24,
                new BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/solicitudes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(crearSolicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", containsString("inactivo")));
    }

    @Test
    @DisplayName("Control de acceso: AFILIADO no puede crear otros afiliados")
    void controlAcceso_AfiliadoNoPuedeCrearAfiliados() throws Exception {
        // Usar usuario afiliado creado en BaseIntegrationTest
        String afiliadoToken = generateAfiliadoToken();

        CrearAfiliadoRequest crearAfiliado = new CrearAfiliadoRequest(
                "555444333",
                "Nuevo Afiliado",
                new BigDecimal("2500000"),
                LocalDate.now().minusMonths(6),
                EstadoAfiliado.ACTIVO
        );

        mockMvc.perform(post("/api/afiliados")
                        .header("Authorization", "Bearer " + afiliadoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(crearAfiliado)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Control de acceso: ANALISTA puede listar solicitudes pero no crear afiliados")
    void controlAcceso_AnalistaPuedeListarPeroNoCrear() throws Exception {
        String analistaToken = generateTokenForUser("analista");

        // Analista puede listar solicitudes
        mockMvc.perform(get("/api/solicitudes")
                        .header("Authorization", "Bearer " + analistaToken))
                .andExpect(status().isOk());

        // Analista NO puede crear afiliados
        CrearAfiliadoRequest crearAfiliado = new CrearAfiliadoRequest(
                "666555444",
                "Intento Analista",
                new BigDecimal("2000000"),
                LocalDate.now().minusMonths(6),
                EstadoAfiliado.ACTIVO
        );

        mockMvc.perform(post("/api/afiliados")
                        .header("Authorization", "Bearer " + analistaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(crearAfiliado)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Validación de datos: Rechazar datos inválidos en múltiples endpoints")
    void validacionDatos_RechazarDatosInvalidos() throws Exception {
        String adminToken = generateTokenForUser("admin");

        // 1. Afiliado con documento inválido
        CrearAfiliadoRequest afiliadoInvalido = new CrearAfiliadoRequest(
                "123", // Muy corto
                "AB", // Muy corto
                new BigDecimal("-1000"), // Negativo
                LocalDate.now().plusDays(1), // Futuro
                null // Estado nulo
        );

        mockMvc.perform(post("/api/afiliados")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(afiliadoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()));

        // 2. Solicitud con monto inválido
        CrearSolicitudRequest solicitudInvalida = new CrearSolicitudRequest(
                1L,
                new BigDecimal("-5000"), // Monto negativo
                0, // Plazo cero
                new BigDecimal("200.00") // Tasa excesiva
        );

        mockMvc.perform(post("/api/solicitudes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(solicitudInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Consultas por afiliado: Listar solicitudes de un afiliado específico")
    void consultasPorAfiliado_ListarSolicitudesDeAfiliado() throws Exception {
        String adminToken = generateTokenForUser("admin");

        // 1. Crear afiliado
        AfiliadoEntity afiliado = new AfiliadoEntity();
        afiliado.setDocumento("777666555");
        afiliado.setNombre("Afiliado Consultas");
        afiliado.setSalario(new BigDecimal("5000000"));
        afiliado.setFechaAfiliacion(LocalDate.now().minusMonths(18));
        afiliado.setEstado(EstadoAfiliado.ACTIVO);
        afiliado = afiliadoRepository.save(afiliado);

        // 2. Crear múltiples solicitudes
        for (int i = 0; i < 3; i++) {
            CrearSolicitudRequest solicitud = new CrearSolicitudRequest(
                    afiliado.getId(),
                    new BigDecimal((i + 1) * 1000000),
                    12 * (i + 1),
                    new BigDecimal("12.00")
            );

            mockMvc.perform(post("/api/solicitudes")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(solicitud)))
                    .andExpect(status().isCreated());
        }

        // 3. Consultar solicitudes del afiliado
        mockMvc.perform(get("/api/solicitudes/afiliado/" + afiliado.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("Actualización de afiliado: Modificar salario y estado")
    void actualizacionAfiliado_ModificarDatos() throws Exception {
        String adminToken = generateTokenForUser("admin");

        // 1. Crear afiliado
        AfiliadoEntity afiliado = new AfiliadoEntity();
        afiliado.setDocumento("888777666");
        afiliado.setNombre("Afiliado Original");
        afiliado.setSalario(new BigDecimal("2000000"));
        afiliado.setFechaAfiliacion(LocalDate.now().minusMonths(12));
        afiliado.setEstado(EstadoAfiliado.ACTIVO);
        afiliado = afiliadoRepository.save(afiliado);

        // 2. Actualizar afiliado
        String actualizacion = """
            {
                "nombre": "Afiliado Actualizado",
                "salario": 3500000,
                "fechaAfiliacion": "%s",
                "estado": "ACTIVO"
            }
            """.formatted(afiliado.getFechaAfiliacion());

        mockMvc.perform(put("/api/afiliados/" + afiliado.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Afiliado Actualizado")))
                .andExpect(jsonPath("$.salario", is(3500000)));

        // 3. Verificar que el cambio persistió
        mockMvc.perform(get("/api/afiliados/" + afiliado.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Afiliado Actualizado")));
    }
}
