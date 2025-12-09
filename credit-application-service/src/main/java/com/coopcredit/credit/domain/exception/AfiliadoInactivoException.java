package com.coopcredit.credit.domain.exception;

/**
 * Excepción lanzada cuando un afiliado inactivo intenta realizar operaciones.
 * 
 * SOLID - LSP: Puede ser sustituida por BusinessRuleViolationException.
 * SOLID - SRP: Solo representa la violación de la regla de afiliado activo.
 */
public class AfiliadoInactivoException extends BusinessRuleViolationException {

    private static final String ENTITY_TYPE = "Afiliado";
    private static final String RULE_CODE = "AFILIADO_INACTIVO";

    private final String documento;

    public AfiliadoInactivoException(String documento) {
        super("El afiliado con documento " + documento + " está inactivo y no puede solicitar créditos",
                ENTITY_TYPE, RULE_CODE);
        this.documento = documento;
    }

    public String getDocumento() {
        return documento;
    }
}
