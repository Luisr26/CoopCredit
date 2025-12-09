package com.coopcredit.credit.domain.policy;

import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.SolicitudCredito;

/**
 * Interfaz para políticas de crédito (Strategy Pattern).
 * 
 * SOLID - OCP: Abierta para extensión (nuevas políticas), cerrada para modificación.
 * SOLID - ISP: Interface pequeña y cohesiva.
 * SOLID - DIP: Los servicios dependen de esta abstracción.
 */
public interface CreditPolicy {

    /**
     * Evalúa si la solicitud cumple con esta política.
     * 
     * @param solicitud solicitud de crédito a evaluar
     * @param afiliado afiliado solicitante
     * @return resultado de la evaluación de la política
     */
    PolicyEvaluationResult evaluate(SolicitudCredito solicitud, Afiliado afiliado);

    /**
     * Obtiene el nombre de la política.
     */
    String getPolicyName();

    /**
     * Obtiene la prioridad de evaluación (menor número = mayor prioridad).
     */
    default int getPriority() {
        return 100;
    }
}
