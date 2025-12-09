package com.coopcredit.credit.domain.policy;

/**
 * Resultado de la evaluación de una política de crédito (Value Object inmutable).
 * 
 * SOLID - SRP: Solo representa el resultado de una evaluación.
 * SOLID - LSP: Puede ser usado polimórficamente en cualquier contexto de evaluación.
 */
public final class PolicyEvaluationResult {

    private final String policyName;
    private final boolean passed;
    private final String message;
    private final Object details;

    private PolicyEvaluationResult(String policyName, boolean passed, String message, Object details) {
        this.policyName = policyName;
        this.passed = passed;
        this.message = message;
        this.details = details;
    }

    /**
     * Crea un resultado de política aprobada.
     */
    public static PolicyEvaluationResult pass(String policyName, String message) {
        return new PolicyEvaluationResult(policyName, true, message, null);
    }

    /**
     * Crea un resultado de política rechazada.
     */
    public static PolicyEvaluationResult fail(String policyName, String message) {
        return new PolicyEvaluationResult(policyName, false, message, null);
    }

    /**
     * Crea un resultado de política rechazada con detalles adicionales.
     */
    public static PolicyEvaluationResult fail(String policyName, String message, Object details) {
        return new PolicyEvaluationResult(policyName, false, message, details);
    }

    public String getPolicyName() {
        return policyName;
    }

    public boolean isPassed() {
        return passed;
    }

    public boolean isFailed() {
        return !passed;
    }

    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return String.format("PolicyEvaluationResult{policy='%s', passed=%s, message='%s'}",
                policyName, passed, message);
    }
}
