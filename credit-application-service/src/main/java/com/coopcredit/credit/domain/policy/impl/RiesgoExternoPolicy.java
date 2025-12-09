package com.coopcredit.credit.domain.policy.impl;

import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.NivelRiesgo;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import com.coopcredit.credit.domain.policy.CreditPolicy;
import com.coopcredit.credit.domain.policy.PolicyEvaluationResult;
import org.springframework.stereotype.Component;

/**
 * Política de riesgo externo.
 * 
 * SOLID - SRP: Solo evalúa el nivel de riesgo externo.
 * SOLID - OCP: Implementa CreditPolicy sin modificar otras políticas.
 * 
 * Nota: Esta política requiere que la evaluación de riesgo externa
 * ya haya sido realizada y esté disponible en el contexto.
 */
@Component
public class RiesgoExternoPolicy implements CreditPolicy {

    private static final String POLICY_NAME = "RIESGO_EXTERNO";

    @Override
    public PolicyEvaluationResult evaluate(SolicitudCredito solicitud, Afiliado afiliado) {
        // Esta política se evalúa con el resultado del servicio externo
        // que se pasa como contexto adicional. Por defecto, aprueba.
        return PolicyEvaluationResult.pass(POLICY_NAME, 
                "Evaluación de riesgo externo pendiente o aprobada");
    }

    /**
     * Evalúa el nivel de riesgo externo.
     */
    public PolicyEvaluationResult evaluateRisk(NivelRiesgo nivelRiesgo, Integer score) {
        if (nivelRiesgo == NivelRiesgo.ALTO) {
            return PolicyEvaluationResult.fail(POLICY_NAME,
                    String.format("Score de riesgo ALTO (%d). No cumple con el perfil de riesgo aceptable.", score),
                    nivelRiesgo);
        }

        String message = String.format("Nivel de riesgo: %s (Score: %d)", nivelRiesgo, score);
        return PolicyEvaluationResult.pass(POLICY_NAME, message);
    }

    @Override
    public String getPolicyName() {
        return POLICY_NAME;
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
