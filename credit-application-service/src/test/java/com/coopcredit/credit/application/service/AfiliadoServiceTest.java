package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.AfiliadoDTO;
import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;
import com.coopcredit.credit.application.mapper.AfiliadoMapper;
import com.coopcredit.credit.application.port.out.AfiliadoRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoNoEncontradoException;
import com.coopcredit.credit.domain.exception.DocumentoDuplicadoException;
import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.EstadoAfiliado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AfiliadoService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AfiliadoService Tests")
class AfiliadoServiceTest {

    @Mock
    private AfiliadoRepositoryPort afiliadoRepository;

    @Mock
    private AfiliadoMapper afiliadoMapper;

    @InjectMocks
    private AfiliadoService afiliadoService;

    private Afiliado afiliadoActivo;
    private AfiliadoDTO afiliadoDTO;
    private CrearAfiliadoRequest crearAfiliadoRequest;

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

        afiliadoDTO = AfiliadoDTO.builder()
                .id(1L)
                .documento("123456789")
                .nombre("Juan Pérez")
                .salario(new BigDecimal("3000000"))
                .fechaAfiliacion(LocalDate.now().minusMonths(12))
                .estado(EstadoAfiliado.ACTIVO)
                .mesesAntiguedad(12L)
                .puedeRecibirCredito(true)
                .build();

        crearAfiliadoRequest = new CrearAfiliadoRequest(
                "123456789",
                "Juan Pérez",
                new BigDecimal("3000000"),
                LocalDate.now().minusMonths(12),
                EstadoAfiliado.ACTIVO
        );
    }

    @Nested
    @DisplayName("Tests para crear()")
    class CrearTests {

        @Test
        @DisplayName("Debe crear afiliado exitosamente cuando documento no existe")
        void crear_DebeCrearAfiliadoExitosamente() {
            // Given
            when(afiliadoRepository.existePorDocumento(anyString())).thenReturn(false);
            when(afiliadoMapper.toDomain(any(CrearAfiliadoRequest.class))).thenReturn(afiliadoActivo);
            when(afiliadoRepository.guardar(any(Afiliado.class))).thenReturn(afiliadoActivo);
            when(afiliadoMapper.toDTO(any(Afiliado.class))).thenReturn(afiliadoDTO);

            // When
            AfiliadoDTO resultado = afiliadoService.crear(crearAfiliadoRequest);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDocumento()).isEqualTo("123456789");
            assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(resultado.getEstado()).isEqualTo(EstadoAfiliado.ACTIVO);

            verify(afiliadoRepository).existePorDocumento("123456789");
            verify(afiliadoRepository).guardar(any(Afiliado.class));
        }

        @Test
        @DisplayName("Debe lanzar DocumentoDuplicadoException cuando documento ya existe")
        void crear_DebeLanzarExcepcionCuandoDocumentoExiste() {
            // Given
            when(afiliadoRepository.existePorDocumento("123456789")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> afiliadoService.crear(crearAfiliadoRequest))
                    .isInstanceOf(DocumentoDuplicadoException.class);

            verify(afiliadoRepository).existePorDocumento("123456789");
            verify(afiliadoRepository, never()).guardar(any(Afiliado.class));
        }
    }

    @Nested
    @DisplayName("Tests para actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("Debe actualizar afiliado existente")
        void actualizar_DebeActualizarAfiliadoExistente() {
            // Given
            when(afiliadoRepository.buscarPorId(1L)).thenReturn(Optional.of(afiliadoActivo));
            when(afiliadoRepository.guardar(any(Afiliado.class))).thenReturn(afiliadoActivo);
            when(afiliadoMapper.toDTO(any(Afiliado.class))).thenReturn(afiliadoDTO);

            AfiliadoDTO actualizacion = AfiliadoDTO.builder()
                    .nombre("Juan Pérez Actualizado")
                    .salario(new BigDecimal("3500000"))
                    .fechaAfiliacion(LocalDate.now().minusMonths(12))
                    .estado(EstadoAfiliado.ACTIVO)
                    .build();

            // When
            AfiliadoDTO resultado = afiliadoService.actualizar(1L, actualizacion);

            // Then
            assertThat(resultado).isNotNull();
            verify(afiliadoRepository).buscarPorId(1L);
            verify(afiliadoRepository).guardar(any(Afiliado.class));
        }

        @Test
        @DisplayName("Debe lanzar AfiliadoNoEncontradoException cuando afiliado no existe")
        void actualizar_DebeLanzarExcepcionCuandoNoExiste() {
            // Given
            when(afiliadoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> afiliadoService.actualizar(99L, afiliadoDTO))
                    .isInstanceOf(AfiliadoNoEncontradoException.class);

            verify(afiliadoRepository).buscarPorId(99L);
            verify(afiliadoRepository, never()).guardar(any(Afiliado.class));
        }
    }

    @Nested
    @DisplayName("Tests para obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar afiliado cuando existe")
        void obtenerPorId_DebeRetornarAfiliadoCuandoExiste() {
            // Given
            when(afiliadoRepository.buscarPorId(1L)).thenReturn(Optional.of(afiliadoActivo));
            when(afiliadoMapper.toDTO(afiliadoActivo)).thenReturn(afiliadoDTO);

            // When
            AfiliadoDTO resultado = afiliadoService.obtenerPorId(1L);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getDocumento()).isEqualTo("123456789");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando afiliado no existe")
        void obtenerPorId_DebeLanzarExcepcionCuandoNoExiste() {
            // Given
            when(afiliadoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> afiliadoService.obtenerPorId(99L))
                    .isInstanceOf(AfiliadoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Tests para obtenerPorDocumento()")
    class ObtenerPorDocumentoTests {

        @Test
        @DisplayName("Debe retornar afiliado cuando documento existe")
        void obtenerPorDocumento_DebeRetornarAfiliadoCuandoExiste() {
            // Given
            when(afiliadoRepository.buscarPorDocumento("123456789")).thenReturn(Optional.of(afiliadoActivo));
            when(afiliadoMapper.toDTO(afiliadoActivo)).thenReturn(afiliadoDTO);

            // When
            AfiliadoDTO resultado = afiliadoService.obtenerPorDocumento("123456789");

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDocumento()).isEqualTo("123456789");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando documento no existe")
        void obtenerPorDocumento_DebeLanzarExcepcionCuandoNoExiste() {
            // Given
            when(afiliadoRepository.buscarPorDocumento("999999999")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> afiliadoService.obtenerPorDocumento("999999999"))
                    .isInstanceOf(AfiliadoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Tests para listarTodos()")
    class ListarTodosTests {

        @Test
        @DisplayName("Debe retornar lista de afiliados")
        void listarTodos_DebeRetornarListaDeAfiliados() {
            // Given
            Afiliado afiliado2 = new Afiliado(
                    2L, "987654321", "María López",
                    new BigDecimal("4000000"),
                    LocalDate.now().minusMonths(24),
                    EstadoAfiliado.ACTIVO
            );

            when(afiliadoRepository.listarTodos()).thenReturn(Arrays.asList(afiliadoActivo, afiliado2));
            when(afiliadoMapper.toDTO(any(Afiliado.class))).thenReturn(afiliadoDTO);

            // When
            List<AfiliadoDTO> resultado = afiliadoService.listarTodos();

            // Then
            assertThat(resultado).hasSize(2);
            verify(afiliadoRepository).listarTodos();
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay afiliados")
        void listarTodos_DebeRetornarListaVaciaCuandoNoHayAfiliados() {
            // Given
            when(afiliadoRepository.listarTodos()).thenReturn(List.of());

            // When
            List<AfiliadoDTO> resultado = afiliadoService.listarTodos();

            // Then
            assertThat(resultado).isEmpty();
        }
    }
}
