package com.coopcredit.credit.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para PoliticasCreditoService.
 * Verifica los cálculos de cuotas, relaciones y validaciones de políticas.
 */
@DisplayName("PoliticasCreditoService Tests")
class PoliticasCreditoServiceTest {

    private PoliticasCreditoService politicasService;

    @BeforeEach
    void setUp() {
        politicasService = new PoliticasCreditoService();
        
        // Inyectar valores de configuración usando ReflectionTestUtils
        ReflectionTestUtils.setField(politicasService, "relacionCuotaIngresoMaxima", new BigDecimal("0.40"));
        ReflectionTestUtils.setField(politicasService, "multiplicadorSalarioMontoMaximo", 5);
        ReflectionTestUtils.setField(politicasService, "antiguedadMinimaMeses", 6);
    }

    @Nested
    @DisplayName("Tests para calcularCuotaMensual()")
    class CalcularCuotaMensualTests {

        @Test
        @DisplayName("Debe calcular cuota mensual correctamente con tasa y plazo")
        void calcularCuotaMensual_DebeCalcularCorrectamente() {
            // Given
            BigDecimal monto = new BigDecimal("10000000");
            BigDecimal tasaAnual = new BigDecimal("12.00"); // 12% anual
            Integer plazoMeses = 12;

            // When
            BigDecimal cuota = politicasService.calcularCuotaMensual(monto, tasaAnual, plazoMeses);

            // Then
            assertThat(cuota).isNotNull();
            assertThat(cuota).isGreaterThan(BigDecimal.ZERO);
            // La cuota mensual debe ser aproximadamente 888,488 para estos valores
            assertThat(cuota).isBetween(new BigDecimal("880000"), new BigDecimal("900000"));
        }

        @Test
        @DisplayName("Debe retornar monto completo cuando plazo es cero")
        void calcularCuotaMensual_DebeRetornarMontoCompletoCuandoPlazoEsCero() {
            // Given
            BigDecimal monto = new BigDecimal("5000000");
            BigDecimal tasaAnual = new BigDecimal("15.00");
            Integer plazoMeses = 0;

            // When
            BigDecimal cuota = politicasService.calcularCuotaMensual(monto, tasaAnual, plazoMeses);

            // Then
            assertThat(cuota).isEqualByComparingTo(monto);
        }

        @Test
        @DisplayName("Debe calcular cuota sin interés cuando tasa es cero")
        void calcularCuotaMensual_DebeCalcularSinInteresCuandoTasaEsCero() {
            // Given
            BigDecimal monto = new BigDecimal("12000000");
            BigDecimal tasaAnual = BigDecimal.ZERO;
            Integer plazoMeses = 12;

            // When
            BigDecimal cuota = politicasService.calcularCuotaMensual(monto, tasaAnual, plazoMeses);

            // Then
            assertThat(cuota).isEqualByComparingTo(new BigDecimal("1000000")); // 12M / 12 meses
        }

        @Test
        @DisplayName("Debe calcular cuota para plazo largo (360 meses)")
        void calcularCuotaMensual_DebeCalcularParaPlazoLargo() {
            // Given
            BigDecimal monto = new BigDecimal("100000000");
            BigDecimal tasaAnual = new BigDecimal("10.00");
            Integer plazoMeses = 360; // 30 años

            // When
            BigDecimal cuota = politicasService.calcularCuotaMensual(monto, tasaAnual, plazoMeses);

            // Then
            assertThat(cuota).isNotNull();
            assertThat(cuota).isGreaterThan(BigDecimal.ZERO);
            // La cuota debe ser menor que si fuera a 12 meses
            assertThat(cuota).isLessThan(monto.divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP));
        }
    }

    @Nested
    @DisplayName("Tests para calcularRelacionCuotaIngreso()")
    class CalcularRelacionCuotaIngresoTests {

        @Test
        @DisplayName("Debe calcular relación cuota/ingreso correctamente")
        void calcularRelacionCuotaIngreso_DebeCalcularCorrectamente() {
            // Given
            BigDecimal cuota = new BigDecimal("500000");
            BigDecimal ingreso = new BigDecimal("2500000");

            // When
            BigDecimal relacion = politicasService.calcularRelacionCuotaIngreso(cuota, ingreso);

            // Then
            assertThat(relacion).isEqualByComparingTo(new BigDecimal("0.2000")); // 20%
        }

        @Test
        @DisplayName("Debe retornar cero cuando ingreso es cero")
        void calcularRelacionCuotaIngreso_DebeRetornarCeroCuandoIngresoEsCero() {
            // Given
            BigDecimal cuota = new BigDecimal("500000");
            BigDecimal ingreso = BigDecimal.ZERO;

            // When
            BigDecimal relacion = politicasService.calcularRelacionCuotaIngreso(cuota, ingreso);

            // Then
            assertThat(relacion).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Debe calcular relación mayor a 1 cuando cuota excede ingreso")
        void calcularRelacionCuotaIngreso_DebeCalcularMayorAUnoCuandoCuotaExcedeIngreso() {
            // Given
            BigDecimal cuota = new BigDecimal("3000000");
            BigDecimal ingreso = new BigDecimal("2000000");

            // When
            BigDecimal relacion = politicasService.calcularRelacionCuotaIngreso(cuota, ingreso);

            // Then
            assertThat(relacion).isEqualByComparingTo(new BigDecimal("1.5000")); // 150%
        }
    }

    @Nested
    @DisplayName("Tests para cumpleRelacionCuotaIngreso()")
    class CumpleRelacionCuotaIngresoTests {

        @Test
        @DisplayName("Debe retornar true cuando relación es menor al máximo")
        void cumpleRelacionCuotaIngreso_DebeRetornarTrueCuandoMenorAlMaximo() {
            // Given
            BigDecimal relacion = new BigDecimal("0.30"); // 30% < 40%

            // When
            boolean resultado = politicasService.cumpleRelacionCuotaIngreso(relacion);

            // Then
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar true cuando relación es igual al máximo")
        void cumpleRelacionCuotaIngreso_DebeRetornarTrueCuandoIgualAlMaximo() {
            // Given
            BigDecimal relacion = new BigDecimal("0.40"); // 40% = 40%

            // When
            boolean resultado = politicasService.cumpleRelacionCuotaIngreso(relacion);

            // Then
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando relación excede el máximo")
        void cumpleRelacionCuotaIngreso_DebeRetornarFalseCuandoExcedeMaximo() {
            // Given
            BigDecimal relacion = new BigDecimal("0.50"); // 50% > 40%

            // When
            boolean resultado = politicasService.cumpleRelacionCuotaIngreso(relacion);

            // Then
            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests para cumpleMontoMaximo()")
    class CumpleMontoMaximoTests {

        @Test
        @DisplayName("Debe retornar true cuando monto es menor al máximo según salario")
        void cumpleMontoMaximo_DebeRetornarTrueCuandoMenorAlMaximo() {
            // Given
            BigDecimal monto = new BigDecimal("10000000");
            BigDecimal salario = new BigDecimal("3000000"); // Máximo: 15M

            // When
            boolean resultado = politicasService.cumpleMontoMaximo(monto, salario);

            // Then
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar true cuando monto es igual al máximo")
        void cumpleMontoMaximo_DebeRetornarTrueCuandoIgualAlMaximo() {
            // Given
            BigDecimal monto = new BigDecimal("15000000");
            BigDecimal salario = new BigDecimal("3000000"); // Máximo: 15M (5x salario)

            // When
            boolean resultado = politicasService.cumpleMontoMaximo(monto, salario);

            // Then
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando monto excede el máximo")
        void cumpleMontoMaximo_DebeRetornarFalseCuandoExcedeMaximo() {
            // Given
            BigDecimal monto = new BigDecimal("20000000");
            BigDecimal salario = new BigDecimal("3000000"); // Máximo: 15M

            // When
            boolean resultado = politicasService.cumpleMontoMaximo(monto, salario);

            // Then
            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests para cumpleAntiguedadMinima()")
    class CumpleAntiguedadMinimaTests {

        @Test
        @DisplayName("Debe retornar true cuando antigüedad es mayor al mínimo")
        void cumpleAntiguedadMinima_DebeRetornarTrueCuandoMayorAlMinimo() {
            // Given
            long mesesAntiguedad = 12; // 12 meses > 6 meses

            // When
            boolean resultado = politicasService.cumpleAntiguedadMinima(mesesAntiguedad);

            // Then
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar true cuando antigüedad es igual al mínimo")
        void cumpleAntiguedadMinima_DebeRetornarTrueCuandoIgualAlMinimo() {
            // Given
            long mesesAntiguedad = 6;

            // When
            boolean resultado = politicasService.cumpleAntiguedadMinima(mesesAntiguedad);

            // Then
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando antigüedad es menor al mínimo")
        void cumpleAntiguedadMinima_DebeRetornarFalseCuandoMenorAlMinimo() {
            // Given
            long mesesAntiguedad = 3; // 3 meses < 6 meses

            // When
            boolean resultado = politicasService.cumpleAntiguedadMinima(mesesAntiguedad);

            // Then
            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests para getters de configuración")
    class GettersConfiguracionTests {

        @Test
        @DisplayName("Debe retornar relación cuota/ingreso máxima configurada")
        void getRelacionCuotaIngresoMaxima_DebeRetornarValorConfigurado() {
            assertThat(politicasService.getRelacionCuotaIngresoMaxima())
                    .isEqualByComparingTo(new BigDecimal("0.40"));
        }

        @Test
        @DisplayName("Debe retornar multiplicador de salario configurado")
        void getMultiplicadorSalarioMontoMaximo_DebeRetornarValorConfigurado() {
            assertThat(politicasService.getMultiplicadorSalarioMontoMaximo()).isEqualTo(5);
        }

        @Test
        @DisplayName("Debe retornar antigüedad mínima configurada")
        void getAntiguedadMinimaMeses_DebeRetornarValorConfigurado() {
            assertThat(politicasService.getAntiguedadMinimaMeses()).isEqualTo(6);
        }
    }
}
