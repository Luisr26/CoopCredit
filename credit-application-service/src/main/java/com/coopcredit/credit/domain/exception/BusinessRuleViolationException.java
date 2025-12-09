package com.coopcredit.credit.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio.
 * 
 * SOLID - OCP: Permite extender con reglas de negocio específicas
 * sin modificar el código existente.
 */
public abstract class BusinessRuleViolationException extends DomainException {

    private final String ruleCode;

    protected BusinessRuleViolationException(String message, String entityType, String ruleCode) {
        super(message, "BUSINESS_RULE_VIOLATION", entityType);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
