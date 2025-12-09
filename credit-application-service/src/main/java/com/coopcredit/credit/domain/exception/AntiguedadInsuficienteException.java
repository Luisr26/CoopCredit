package com.coopcredit.credit.domain.exception;

/**
 * Excepción lanzada cuando un afiliado no tiene la antigüedad mínima requerida.
 * 
 * SOLID - LSP: Puede ser sustituida por BusinessRuleViolationException.
 * SOLID - SRP: Solo representa la violación de la regla de antigüedad mínima.
 */
public class AntiguedadInsuficienteException extends BusinessRuleViolationException {

    private static final String ENTITY_TYPE = "Afiliado";
    private static final String RULE_CODE = "ANTIGUEDAD_INSUFICIENTE";

    private final long mesesActuales;
    private final int mesesRequeridos;

    public AntiguedadInsuficienteException(long mesesActuales, int mesesRequeridos) {
        super(String.format("Antigüedad insuficiente. Actual: %d meses. Requerido: %d meses",
                mesesActuales, mesesRequeridos), ENTITY_TYPE, RULE_CODE);
        this.mesesActuales = mesesActuales;
        this.mesesRequeridos = mesesRequeridos;
    }

    public long getMesesActuales() {
        return mesesActuales;
    }

    public int getMesesRequeridos() {
        return mesesRequeridos;
    }
}
