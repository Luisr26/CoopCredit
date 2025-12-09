package com.coopcredit.credit.domain.exception;

/**
 * Excepción lanzada cuando una solicitud de crédito es rechazada.
 * 
 * SOLID - LSP: Puede ser sustituida por BusinessRuleViolationException.
 * SOLID - SRP: Solo representa el rechazo de una solicitud de crédito.
 */
public class CreditoRechazadoException extends BusinessRuleViolationException {

    private static final String ENTITY_TYPE = "SolicitudCredito";
    private static final String RULE_CODE = "CREDITO_RECHAZADO";

    private final String motivo;

    public CreditoRechazadoException(String motivo) {
        super("Solicitud de crédito rechazada: " + motivo, ENTITY_TYPE, RULE_CODE);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
