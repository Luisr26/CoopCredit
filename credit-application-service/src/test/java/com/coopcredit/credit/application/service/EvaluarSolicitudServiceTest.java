package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.application.mapper.SolicitudCreditoMapper;
import com.coopcredit.credit.application.port.out.RiskCentralPort;
import com.coopcredit.credit.application.port.out.RiskCentralPort.RiskEvaluationResponse;
import com.coopcredit.credit.application.port.out.SolicitudCreditoRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoInactivoException;
import com.coopcredit.credit.domain.exception.AntiguedadInsuficienteException;
import com.coopcredit.credit.domain.exception.SolicitudNoEncontradaException;
import com.coopcredit.credit.domain.model.*;
import com.coopcredit.credit.infrastructure.metrics.CreditApplicationMetrics;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EvaluarSolicitudService.
 * Este es el servicio más crítico que integra evaluación de riesgo externa con políticas internas.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EvaluarSolicitudService Tests")
class EvaluarSolicitudServiceTest {

    @Mock
    private SolicitudCreditoRepositoryPort solicitudRepository;

    @Mock
    private RiskCentralPort riskCentralPort;

    @Mock
    private PoliticasCreditoService politicasService;

    @Mock
    private SolicitudCreditoMapper solicitudMapper;

    @Mock
    private CreditApplicationMetrics metrics;

    @InjectMocks
    private EvaluarSolicitudService evaluarSolicitudService;

    private Afiliado afiliadoActivo;
    private Afiliado afiliadoInactivo;
    private Afiliado afiliadoSinAntiguedad;
    private SolicitudCredito solicitudPendiente;
    private SolicitudCreditoDTO solicitudDTO;

    @BeforeEach
    void setUp() {
        // Afiliado activo con antigüedad suficiente
        afiliadoActivo = new Afiliado(
                1L,
                "123456789",
                "Juan Pérez",
                new BigDecimal("3000000"),
                LocalDate.now().minusMonths(12),
                EstadoAfiliado.ACTIVO
        );

        // Afiliado inactivo
        afiliadoInactivo = new Afiliado(
                2L,
                "987654321",
                "María López",
                new BigDecimal("2500000"),
                LocalDate.now().minusMonths(24),
                EstadoAfiliado.INACTIVO
        );

        // Afiliado sin antigüedad mínima
        afiliadoSinAntiguedad = new Afiliado(
                3L,
                "555555555",
                "Pedro García",
                new BigDecimal("2000000"),
                LocalDate.now().minusMonths(2),
                EstadoAfiliado.ACTIVO
        );

        // Solicitud pendiente de evaluación
        solicitudPendiente = new SolicitudCredito();
        solicitudPendiente.setId(1L);
        solicitudPendiente.setAfiliado(afiliadoActivo);
        solicitudPendiente.setMonto(new BigDecimal("5000000"));
        solicitudPendiente.setPlazoMeses(24);
        solicitudPendiente.setTasaPropuesta(new BigDecimal("15.00"));
        solicitudPendiente.setFechaSolicitud(LocalDateTime.now());
        solicitudPendiente.setEstado(EstadoSolicitud.PENDIENTE);

        solicitudDTO = SolicitudCreditoDTO.builder()
                .id(1L)
                .afiliadoId(1L)
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .estado(EstadoSolicitud.APROBADO)
                .build();

        // Configurar mock de metrics
        Timer.Sample mockSample = mock(Timer.Sample.class);
        when(metrics.iniciarMedicionTiempo()).thenReturn(mockSample);
    }

    @Nested
    @DisplayName("Tests de evaluación exitosa")
    class EvaluacionExitosaTests {

        @Test
        @DisplayName("Debe aprobar solicitud cuando cumple todas las políticas")
        void evaluar_DebeAprobarCuandoCumplePoliticas() {
            // Given
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            
            RiskEvaluationResponse riskResponse = new RiskEvaluationResponse(
                    "123456789", 750, "BAJO", "Score favorable"
            );
            when(riskCentralPort.evaluarRiesgo(anyString(), any(BigDecimal.class), anyInt()))
                    .thenReturn(riskResponse);

            // Configurar políticas
            when(politicasService.getAntiguedadMinimaMeses()).thenReturn(6);
            when(politicasService.calcularCuotaMensual(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("250000"));
            when(politicasService.calcularRelacionCuotaIngreso(any(), any()))
                    .thenReturn(new BigDecimal("0.08")); // 8% < 40%
            when(politicasService.cumpleRelacionCuotaIngreso(any())).thenReturn(true);
            when(politicasService.cumpleMontoMaximo(any(), any())).thenReturn(true);
            when(politicasService.getRelacionCuotaIngresoMaxima()).thenReturn(new BigDecimal("0.40"));
            when(politicasService.getMultiplicadorSalarioMontoMaximo()).thenReturn(5);

            when(solicitudRepository.guardar(any(SolicitudCredito.class))).thenReturn(solicitudPendiente);
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            SolicitudCreditoDTO resultado = evaluarSolicitudService.evaluar(1L);

            // Then
            assertThat(resultado).isNotNull();
            
            ArgumentCaptor<SolicitudCredito> captor = ArgumentCaptor.forClass(SolicitudCredito.class);
            verify(solicitudRepository).guardar(captor.capture());
            
            SolicitudCredito solicitudGuardada = captor.getValue();
            assertThat(solicitudGuardada.getEstado()).isEqualTo(EstadoSolicitud.APROBADO);
            assertThat(solicitudGuardada.getEvaluacion()).isNotNull();
            assertThat(solicitudGuardada.getEvaluacion().getAprobado()).isTrue();
        }

        @Test
        @DisplayName("Debe rechazar solicitud cuando riesgo es ALTO")
        void evaluar_DebeRechazarCuandoRiesgoAlto() {
            // Given
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            
            RiskEvaluationResponse riskResponse = new RiskEvaluationResponse(
                    "123456789", 400, "ALTO", "Score desfavorable"
            );
            when(riskCentralPort.evaluarRiesgo(anyString(), any(BigDecimal.class), anyInt()))
                    .thenReturn(riskResponse);

            // Configurar políticas (todas cumplen excepto el riesgo)
            when(politicasService.getAntiguedadMinimaMeses()).thenReturn(6);
            when(politicasService.calcularCuotaMensual(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("250000"));
            when(politicasService.calcularRelacionCuotaIngreso(any(), any()))
                    .thenReturn(new BigDecimal("0.08"));
            when(politicasService.cumpleRelacionCuotaIngreso(any())).thenReturn(true);
            when(politicasService.cumpleMontoMaximo(any(), any())).thenReturn(true);

            when(solicitudRepository.guardar(any(SolicitudCredito.class))).thenReturn(solicitudPendiente);
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            evaluarSolicitudService.evaluar(1L);

            // Then
            ArgumentCaptor<SolicitudCredito> captor = ArgumentCaptor.forClass(SolicitudCredito.class);
            verify(solicitudRepository).guardar(captor.capture());
            
            SolicitudCredito solicitudGuardada = captor.getValue();
            assertThat(solicitudGuardada.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADO);
            assertThat(solicitudGuardada.getEvaluacion().getAprobado()).isFalse();
            assertThat(solicitudGuardada.getEvaluacion().getMotivo()).contains("ALTO");
        }

        @Test
        @DisplayName("Debe rechazar solicitud cuando relación cuota/ingreso excede máximo")
        void evaluar_DebeRechazarCuandoRelacionCuotaIngresoExcede() {
            // Given
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            
            RiskEvaluationResponse riskResponse = new RiskEvaluationResponse(
                    "123456789", 700, "MEDIO", "Score aceptable"
            );
            when(riskCentralPort.evaluarRiesgo(anyString(), any(BigDecimal.class), anyInt()))
                    .thenReturn(riskResponse);

            // Configurar políticas - relación cuota/ingreso excede
            when(politicasService.getAntiguedadMinimaMeses()).thenReturn(6);
            when(politicasService.calcularCuotaMensual(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("1500000"));
            when(politicasService.calcularRelacionCuotaIngreso(any(), any()))
                    .thenReturn(new BigDecimal("0.50")); // 50% > 40%
            when(politicasService.cumpleRelacionCuotaIngreso(any())).thenReturn(false);
            when(politicasService.cumpleMontoMaximo(any(), any())).thenReturn(true);
            when(politicasService.getRelacionCuotaIngresoMaxima()).thenReturn(new BigDecimal("0.40"));

            when(solicitudRepository.guardar(any(SolicitudCredito.class))).thenReturn(solicitudPendiente);
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            evaluarSolicitudService.evaluar(1L);

            // Then
            ArgumentCaptor<SolicitudCredito> captor = ArgumentCaptor.forClass(SolicitudCredito.class);
            verify(solicitudRepository).guardar(captor.capture());
            
            SolicitudCredito solicitudGuardada = captor.getValue();
            assertThat(solicitudGuardada.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADO);
            assertThat(solicitudGuardada.getEvaluacion().getAprobado()).isFalse();
        }

        @Test
        @DisplayName("Debe rechazar solicitud cuando monto excede máximo según salario")
        void evaluar_DebeRechazarCuandoMontoExcedeMaximo() {
            // Given
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            
            RiskEvaluationResponse riskResponse = new RiskEvaluationResponse(
                    "123456789", 700, "MEDIO", "Score aceptable"
            );
            when(riskCentralPort.evaluarRiesgo(anyString(), any(BigDecimal.class), anyInt()))
                    .thenReturn(riskResponse);

            // Configurar políticas - monto excede máximo
            when(politicasService.getAntiguedadMinimaMeses()).thenReturn(6);
            when(politicasService.calcularCuotaMensual(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("250000"));
            when(politicasService.calcularRelacionCuotaIngreso(any(), any()))
                    .thenReturn(new BigDecimal("0.08"));
            when(politicasService.cumpleRelacionCuotaIngreso(any())).thenReturn(true);
            when(politicasService.cumpleMontoMaximo(any(), any())).thenReturn(false);
            when(politicasService.getMultiplicadorSalarioMontoMaximo()).thenReturn(5);

            when(solicitudRepository.guardar(any(SolicitudCredito.class))).thenReturn(solicitudPendiente);
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            evaluarSolicitudService.evaluar(1L);

            // Then
            ArgumentCaptor<SolicitudCredito> captor = ArgumentCaptor.forClass(SolicitudCredito.class);
            verify(solicitudRepository).guardar(captor.capture());
            
            SolicitudCredito solicitudGuardada = captor.getValue();
            assertThat(solicitudGuardada.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADO);
        }
    }

    @Nested
    @DisplayName("Tests de validaciones previas")
    class ValidacionesPreviasTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando solicitud no existe")
        void evaluar_DebeLanzarExcepcionCuandoSolicitudNoExiste() {
            // Given
            when(solicitudRepository.buscarPorId(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> evaluarSolicitudService.evaluar(99L))
                    .isInstanceOf(SolicitudNoEncontradaException.class);

            verify(riskCentralPort, never()).evaluarRiesgo(anyString(), any(), anyInt());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando solicitud ya fue evaluada")
        void evaluar_DebeLanzarExcepcionCuandoYaFueEvaluada() {
            // Given
            solicitudPendiente.setEstado(EstadoSolicitud.APROBADO);
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));

            // When/Then
            assertThatThrownBy(() -> evaluarSolicitudService.evaluar(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya ha sido evaluada");

            verify(riskCentralPort, never()).evaluarRiesgo(anyString(), any(), anyInt());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando afiliado está inactivo")
        void evaluar_DebeLanzarExcepcionCuandoAfiliadoInactivo() {
            // Given
            solicitudPendiente.setAfiliado(afiliadoInactivo);
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));

            // When/Then
            assertThatThrownBy(() -> evaluarSolicitudService.evaluar(1L))
                    .isInstanceOf(AfiliadoInactivoException.class);

            verify(riskCentralPort, never()).evaluarRiesgo(anyString(), any(), anyInt());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando afiliado no tiene antigüedad mínima")
        void evaluar_DebeLanzarExcepcionCuandoSinAntiguedadMinima() {
            // Given
            solicitudPendiente.setAfiliado(afiliadoSinAntiguedad);
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            when(politicasService.getAntiguedadMinimaMeses()).thenReturn(6);

            // When/Then
            assertThatThrownBy(() -> evaluarSolicitudService.evaluar(1L))
                    .isInstanceOf(AntiguedadInsuficienteException.class);

            verify(riskCentralPort, never()).evaluarRiesgo(anyString(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("Tests de integración con servicio de riesgo")
    class IntegracionRiskCentralTests {

        @Test
        @DisplayName("Debe llamar a servicio de riesgo con parámetros correctos")
        void evaluar_DebeLlamarServicioRiesgoConParametrosCorrectos() {
            // Given
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            
            RiskEvaluationResponse riskResponse = new RiskEvaluationResponse(
                    "123456789", 750, "BAJO", "Score favorable"
            );
            when(riskCentralPort.evaluarRiesgo(anyString(), any(BigDecimal.class), anyInt()))
                    .thenReturn(riskResponse);

            when(politicasService.getAntiguedadMinimaMeses()).thenReturn(6);
            when(politicasService.calcularCuotaMensual(any(), any(), anyInt()))
                    .thenReturn(new BigDecimal("250000"));
            when(politicasService.calcularRelacionCuotaIngreso(any(), any()))
                    .thenReturn(new BigDecimal("0.08"));
            when(politicasService.cumpleRelacionCuotaIngreso(any())).thenReturn(true);
            when(politicasService.cumpleMontoMaximo(any(), any())).thenReturn(true);
            when(politicasService.getRelacionCuotaIngresoMaxima()).thenReturn(new BigDecimal("0.40"));
            when(politicasService.getMultiplicadorSalarioMontoMaximo()).thenReturn(5);

            when(solicitudRepository.guardar(any(SolicitudCredito.class))).thenReturn(solicitudPendiente);
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            evaluarSolicitudService.evaluar(1L);

            // Then
            verify(riskCentralPort).evaluarRiesgo(
                    eq("123456789"),
                    eq(new BigDecimal("5000000")),
                    eq(24)
            );
        }
    }
}
