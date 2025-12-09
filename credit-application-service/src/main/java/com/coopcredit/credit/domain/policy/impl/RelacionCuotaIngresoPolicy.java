package com.coopcredit.credit.domain.policy.impl;

import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import com.coopcredit.credit.domain.policy.CreditPolicy;
import com.coopcredit.credit.domain.policy.PolicyEvaluationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Política de relación cuota/ingreso.
 * 
 * SOLID - SRP: Solo evalúa la relación cuota/ingreso.
 * SOLID - OCP: Implementa CreditPolicy sin modificar otras políticas.
 */
@Component
public class RelacionCuotaIngresoPolicy implements CreditPolicy {

    private static final String POLICY_NAME = "RELACION_CUOTA_INGRESO";

    @Value("${coopcredit.politicas.relacion-cuota-ingreso-maxima:0.40}")
    private BigDecimal relacionMaxima;

    @Override
    public PolicyEvaluationResult evaluate(SolicitudCredito solicitud, Afiliado afiliado) {
        BigDecimal cuotaMensual = calcularCuotaMensual(
                solicitud.getMonto(),
                solicitud.getTasaPropuesta(),
                solicitud.getPlazoMeses()
        );

        BigDecimal relacionCuotaIngreso = cuotaMensual.divide(
                afiliado.getSalario(), 4, RoundingMode.HALF_UP
        );

        if (relacionCuotaIngreso.compareTo(relacionMaxima) <= 0) {
            return PolicyEvaluationResult.pass(POLICY_NAME,
                    String.format("Relación cuota/ingreso: %.2f%% (máx: %.2f%%)",
                            relacionCuotaIngreso.multiply(BigDecimal.valueOf(100)),
                            relacionMaxima.multiply(BigDecimal.valueOf(100))));
        } else {
            return PolicyEvaluationResult.fail(POLICY_NAME,
                    String.format("Relación cuota/ingreso excede el máximo: %.2f%% > %.2f%%",
                            relacionCuotaIngreso.multiply(BigDecimal.valueOf(100)),
                            relacionMaxima.multiply(BigDecimal.valueOf(100))),
                    relacionCuotaIngreso);
        }
    }

    @Override
    public String getPolicyName() {
        return POLICY_NAME;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    private BigDecimal calcularCuotaMensual(BigDecimal monto, BigDecimal tasaAnual, Integer plazoMeses) {
        if (plazoMeses == 0) {
            return monto;
        }

        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        BigDecimal unoPlusTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoPlusTasa.pow(plazoMeses);
        BigDecimal numerador = tasaMensual.multiply(potencia);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE);

        return monto.multiply(numerador.divide(denominador, 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
