package com.coopcredit.credit.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para evaluación de políticas de crédito internas.
 */
@Service
public class PoliticasCreditoService {

    @Value("${coopcredit.politicas.relacion-cuota-ingreso-maxima:0.40}")
    private BigDecimal relacionCuotaIngresoMaxima;

    @Value("${coopcredit.politicas.multiplicador-salario-monto-maximo:5}")
    private int multiplicadorSalarioMontoMaximo;

    @Value("${coopcredit.politicas.antiguedad-minima-meses:6}")
    private int antiguedadMinimaMeses;

    /**
     * Calcula la cuota mensual usando la fórmula de amortización francesa.
     */
    public BigDecimal calcularCuotaMensual(BigDecimal monto, BigDecimal tasaAnual, Integer plazoMeses) {
        if (plazoMeses == 0) {
            return monto;
        }

        // Convertir tasa anual a mensual
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        // Fórmula: C = M * [i * (1 + i)^n] / [(1 + i)^n - 1]
        BigDecimal unoPlusTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoPlusTasa.pow(plazoMeses);

        BigDecimal numerador = tasaMensual.multiply(potencia);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE);

        return monto.multiply(numerador.divide(denominador, 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la relación cuota/ingreso.
     */
    public BigDecimal calcularRelacionCuotaIngreso(BigDecimal cuota, BigDecimal ingreso) {
        if (ingreso.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return cuota.divide(ingreso, 4, RoundingMode.HALF_UP);
    }

    /**
     * Valida que la relación cuota/ingreso no exceda el máximo permitido.
     */
    public boolean cumpleRelacionCuotaIngreso(BigDecimal relacionCuotaIngreso) {
        return relacionCuotaIngreso.compareTo(relacionCuotaIngresoMaxima) <= 0;
    }

    /**
     * Valida que el monto no exceda el máximo según el salario del afiliado.
     */
    public boolean cumpleMontoMaximo(BigDecimal monto, BigDecimal salario) {
        BigDecimal montoMaximo = salario.multiply(BigDecimal.valueOf(multiplicadorSalarioMontoMaximo));
        return monto.compareTo(montoMaximo) <= 0;
    }

    /**
     * Valida que el afiliado tenga la antigüedad mínima requerida.
     */
    public boolean cumpleAntiguedadMinima(long mesesAntiguedad) {
        return mesesAntiguedad >= antiguedadMinimaMeses;
    }

    // Getters para las políticas configurables

    public BigDecimal getRelacionCuotaIngresoMaxima() {
        return relacionCuotaIngresoMaxima;
    }

    public int getMultiplicadorSalarioMontoMaximo() {
        return multiplicadorSalarioMontoMaximo;
    }

    public int getAntiguedadMinimaMeses() {
        return antiguedadMinimaMeses;
    }
}
