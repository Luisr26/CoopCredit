package com.coopcredit.credit.domain.policy;

import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluador de políticas de crédito.
 * 
 * SOLID - OCP: Nuevas políticas se agregan automáticamente via inyección de dependencias.
 * SOLID - DIP: Depende de la abstracción CreditPolicy, no de implementaciones concretas.
 * SOLID - SRP: Solo se encarga de evaluar políticas.
 */
@Component
public class CreditPolicyEvaluator {

    private final List<CreditPolicy> policies;

    /**
     * Constructor que inyecta automáticamente todas las implementaciones de CreditPolicy.
     * 
     * SOLID - DIP: Spring inyecta todas las implementaciones de la interfaz.
     */
    public CreditPolicyEvaluator(List<CreditPolicy> policies) {
        this.policies = policies.stream()
                .sorted(Comparator.comparingInt(CreditPolicy::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * Evalúa todas las políticas y retorna los resultados.
     * 
     * @param solicitud solicitud a evaluar
     * @param afiliado afiliado solicitante
     * @return lista de resultados de evaluación
     */
    public List<PolicyEvaluationResult> evaluateAll(SolicitudCredito solicitud, Afiliado afiliado) {
        List<PolicyEvaluationResult> results = new ArrayList<>();

        for (CreditPolicy policy : policies) {
            PolicyEvaluationResult result = policy.evaluate(solicitud, afiliado);
            results.add(result);
        }

        return results;
    }

    /**
     * Evalúa todas las políticas y determina si todas pasaron.
     * 
     * @param solicitud solicitud a evaluar
     * @param afiliado afiliado solicitante
     * @return true si todas las políticas pasaron
     */
    public boolean evaluateAndCheckApproval(SolicitudCredito solicitud, Afiliado afiliado) {
        return evaluateAll(solicitud, afiliado).stream()
                .allMatch(PolicyEvaluationResult::isPassed);
    }

    /**
     * Evalúa todas las políticas y retorna los motivos de rechazo.
     * 
     * @param solicitud solicitud a evaluar
     * @param afiliado afiliado solicitante
     * @return lista de mensajes de rechazo (vacía si todo pasó)
     */
    public List<String> getFailureReasons(SolicitudCredito solicitud, Afiliado afiliado) {
        return evaluateAll(solicitud, afiliado).stream()
                .filter(PolicyEvaluationResult::isFailed)
                .map(PolicyEvaluationResult::getMessage)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las políticas registradas.
     */
    public List<CreditPolicy> getPolicies() {
        return new ArrayList<>(policies);
    }
}
