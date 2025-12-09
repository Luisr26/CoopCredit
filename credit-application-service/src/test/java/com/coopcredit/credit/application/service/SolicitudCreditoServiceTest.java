package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.CrearSolicitudRequest;
import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.application.mapper.SolicitudCreditoMapper;
import com.coopcredit.credit.application.port.out.AfiliadoRepositoryPort;
import com.coopcredit.credit.application.port.out.SolicitudCreditoRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoInactivoException;
import com.coopcredit.credit.domain.exception.AfiliadoNoEncontradoException;
import com.coopcredit.credit.domain.exception.SolicitudNoEncontradaException;
import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.EstadoAfiliado;
import com.coopcredit.credit.domain.model.EstadoSolicitud;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para SolicitudCreditoService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitudCreditoService Tests")
class SolicitudCreditoServiceTest {

    @Mock
    private SolicitudCreditoRepositoryPort solicitudRepository;

    @Mock
    private AfiliadoRepositoryPort afiliadoRepository;

    @Mock
    private SolicitudCreditoMapper solicitudMapper;

    @InjectMocks
    private SolicitudCreditoService solicitudCreditoService;

    private Afiliado afiliadoActivo;
    private Afiliado afiliadoInactivo;
    private SolicitudCredito solicitudPendiente;
    private SolicitudCreditoDTO solicitudDTO;
    private CrearSolicitudRequest crearSolicitudRequest;

    @BeforeEach
    void setUp() {
        afiliadoActivo = new Afiliado(
                1L,
                "123456789",
                "Juan Pérez",
                new BigDecimal("3000000"),
                LocalDate.now().minusMonths(12),
                EstadoAfiliado.ACTIVO
        );

        afiliadoInactivo = new Afiliado(
                2L,
                "987654321",
                "María López",
                new BigDecimal("2500000"),
                LocalDate.now().minusMonths(24),
                EstadoAfiliado.INACTIVO
        );

        solicitudPendiente = new SolicitudCredito();
        solicitudPendiente.setId(1L);
        solicitudPendiente.setAfiliado(afiliadoActivo);
        solicitudPendiente.setMonto(new BigDecimal("5000000"));
        solicitudPendiente.setPlazoMeses(24);
        solicitudPendiente.setTasaPropuesta(new BigDecimal("15.00"));
        solicitudPendiente.setFechaSolicitud(LocalDateTime.now());
        solicitudPendiente.setEstado(EstadoSolicitud.PENDIENTE);

        solicitudDTO = new SolicitudCreditoDTO();
        solicitudDTO.setId(1L);
        solicitudDTO.setAfiliadoId(1L);
        solicitudDTO.setMonto(new BigDecimal("5000000"));
        solicitudDTO.setPlazoMeses(24);
        solicitudDTO.setTasaPropuesta(new BigDecimal("15.00"));
        solicitudDTO.setEstado(EstadoSolicitud.PENDIENTE);

        crearSolicitudRequest = new CrearSolicitudRequest(
                1L,
                new BigDecimal("5000000"),
                24,
                new BigDecimal("15.00")
        );
    }

    @Nested
    @DisplayName("Tests para crear()")
    class CrearTests {

        @Test
        @DisplayName("Debe crear solicitud exitosamente cuando afiliado está activo")
        void crear_DebeCrearSolicitudExitosamente() {
            // Given
            when(afiliadoRepository.buscarPorId(1L)).thenReturn(Optional.of(afiliadoActivo));
            when(solicitudRepository.guardar(any(SolicitudCredito.class))).thenReturn(solicitudPendiente);
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            SolicitudCreditoDTO resultado = solicitudCreditoService.crear(crearSolicitudRequest);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
            assertThat(resultado.getMonto()).isEqualByComparingTo(new BigDecimal("5000000"));

            ArgumentCaptor<SolicitudCredito> captor = ArgumentCaptor.forClass(SolicitudCredito.class);
            verify(solicitudRepository).guardar(captor.capture());

            SolicitudCredito solicitudGuardada = captor.getValue();
            assertThat(solicitudGuardada.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
            assertThat(solicitudGuardada.getAfiliado()).isEqualTo(afiliadoActivo);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando afiliado no existe")
        void crear_DebeLanzarExcepcionCuandoAfiliadoNoExiste() {
            // Given
            when(afiliadoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    99L,
                    new BigDecimal("5000000"),
                    24,
                    new BigDecimal("15.00")
            );

            // When/Then
            assertThatThrownBy(() -> solicitudCreditoService.crear(request))
                    .isInstanceOf(AfiliadoNoEncontradoException.class);

            verify(afiliadoRepository).buscarPorId(99L);
            verify(solicitudRepository, never()).guardar(any(SolicitudCredito.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando afiliado está inactivo")
        void crear_DebeLanzarExcepcionCuandoAfiliadoInactivo() {
            // Given
            when(afiliadoRepository.buscarPorId(2L)).thenReturn(Optional.of(afiliadoInactivo));

            CrearSolicitudRequest request = new CrearSolicitudRequest(
                    2L,
                    new BigDecimal("5000000"),
                    24,
                    new BigDecimal("15.00")
            );

            // When/Then
            assertThatThrownBy(() -> solicitudCreditoService.crear(request))
                    .isInstanceOf(AfiliadoInactivoException.class);

            verify(afiliadoRepository).buscarPorId(2L);
            verify(solicitudRepository, never()).guardar(any(SolicitudCredito.class));
        }
    }

    @Nested
    @DisplayName("Tests para obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar solicitud cuando existe")
        void obtenerPorId_DebeRetornarSolicitudCuandoExiste() {
            // Given
            when(solicitudRepository.buscarPorId(1L)).thenReturn(Optional.of(solicitudPendiente));
            when(solicitudMapper.toDTO(solicitudPendiente)).thenReturn(solicitudDTO);

            // When
            SolicitudCreditoDTO resultado = solicitudCreditoService.obtenerPorId(1L);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando solicitud no existe")
        void obtenerPorId_DebeLanzarExcepcionCuandoNoExiste() {
            // Given
            when(solicitudRepository.buscarPorId(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> solicitudCreditoService.obtenerPorId(99L))
                    .isInstanceOf(SolicitudNoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Tests para listarTodas()")
    class ListarTodasTests {

        @Test
        @DisplayName("Debe retornar lista de solicitudes")
        void listarTodas_DebeRetornarListaDeSolicitudes() {
            // Given
            SolicitudCredito solicitud2 = new SolicitudCredito();
            solicitud2.setId(2L);
            solicitud2.setAfiliado(afiliadoActivo);
            solicitud2.setMonto(new BigDecimal("3000000"));
            solicitud2.setEstado(EstadoSolicitud.APROBADO);

            when(solicitudRepository.listarTodas()).thenReturn(Arrays.asList(solicitudPendiente, solicitud2));
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            List<SolicitudCreditoDTO> resultado = solicitudCreditoService.listarTodas();

            // Then
            assertThat(resultado).hasSize(2);
            verify(solicitudRepository).listarTodas();
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay solicitudes")
        void listarTodas_DebeRetornarListaVacia() {
            // Given
            when(solicitudRepository.listarTodas()).thenReturn(List.of());

            // When
            List<SolicitudCreditoDTO> resultado = solicitudCreditoService.listarTodas();

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests para listarPorAfiliado()")
    class ListarPorAfiliadoTests {

        @Test
        @DisplayName("Debe retornar solicitudes del afiliado")
        void listarPorAfiliado_DebeRetornarSolicitudesDelAfiliado() {
            // Given
            when(solicitudRepository.listarPorAfiliado(1L)).thenReturn(List.of(solicitudPendiente));
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            List<SolicitudCreditoDTO> resultado = solicitudCreditoService.listarPorAfiliado(1L);

            // Then
            assertThat(resultado).hasSize(1);
            verify(solicitudRepository).listarPorAfiliado(1L);
        }
    }

    @Nested
    @DisplayName("Tests para listarPorEstado()")
    class ListarPorEstadoTests {

        @Test
        @DisplayName("Debe retornar solicitudes por estado PENDIENTE")
        void listarPorEstado_DebeRetornarSolicitudesPendientes() {
            // Given
            when(solicitudRepository.listarPorEstado(EstadoSolicitud.PENDIENTE))
                    .thenReturn(List.of(solicitudPendiente));
            when(solicitudMapper.toDTO(any(SolicitudCredito.class))).thenReturn(solicitudDTO);

            // When
            List<SolicitudCreditoDTO> resultado = solicitudCreditoService.listarPorEstado(EstadoSolicitud.PENDIENTE);

            // Then
            assertThat(resultado).hasSize(1);
            verify(solicitudRepository).listarPorEstado(EstadoSolicitud.PENDIENTE);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay solicitudes con el estado")
        void listarPorEstado_DebeRetornarListaVaciaCuandoNoHay() {
            // Given
            when(solicitudRepository.listarPorEstado(EstadoSolicitud.RECHAZADO))
                    .thenReturn(List.of());

            // When
            List<SolicitudCreditoDTO> resultado = solicitudCreditoService.listarPorEstado(EstadoSolicitud.RECHAZADO);

            // Then
            assertThat(resultado).isEmpty();
        }
    }
}
