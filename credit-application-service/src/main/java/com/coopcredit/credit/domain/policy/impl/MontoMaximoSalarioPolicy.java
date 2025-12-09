package com.coopcredit.credit.domain.policy.impl;

import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import com.coopcredit.credit.domain.policy.CreditPolicy;
import com.coopcredit.credit.domain.policy.PolicyEvaluationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Política de monto máximo según salario.
 * 
 * SOLID - SRP: Solo evalúa el monto máximo según salario.
 * SOLID - OCP: Implementa CreditPolicy sin modificar otras políticas.
 */
@Component
public class MontoMaximoSalarioPolicy implements CreditPolicy {

    private static final String POLICY_NAME = "MONTO_MAXIMO_SALARIO";

    @Value("${coopcredit.politicas.multiplicador-salario-monto-maximo:5}")
    private int multiplicadorSalario;

    @Override
    public PolicyEvaluationResult evaluate(SolicitudCredito solicitud, Afiliado afiliado) {
        BigDecimal montoMaximo = afiliado.getSalario()
                .multiply(BigDecimal.valueOf(multiplicadorSalario));

        if (solicitud.getMonto().compareTo(montoMaximo) <= 0) {
            return PolicyEvaluationResult.pass(POLICY_NAME,
                    String.format("Monto solicitado: $%,.2f dentro del máximo: $%,.2f",
                            solicitud.getMonto(), montoMaximo));
        } else {
            return PolicyEvaluationResult.fail(POLICY_NAME,
                    String.format("Monto solicitado excede el máximo: $%,.2f > $%,.2f",
                            solicitud.getMonto(), montoMaximo),
                    montoMaximo);
        }
    }

    @Override
    public String getPolicyName() {
        return POLICY_NAME;
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
