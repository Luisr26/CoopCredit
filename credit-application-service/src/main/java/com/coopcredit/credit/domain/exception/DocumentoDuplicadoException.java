package com.coopcredit.credit.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear un afiliado con documento duplicado.
 * 
 * SOLID - LSP: Puede ser sustituida por BusinessRuleViolationException.
 * SOLID - SRP: Solo representa la violación de la regla de unicidad de documento.
 */
public class DocumentoDuplicadoException extends BusinessRuleViolationException {

    private static final String ENTITY_TYPE = "Afiliado";
    private static final String RULE_CODE = "DOCUMENTO_DUPLICADO";

    private final String documento;

    public DocumentoDuplicadoException(String documento) {
        super("Ya existe un afiliado con el documento: " + documento, ENTITY_TYPE, RULE_CODE);
        this.documento = documento;
    }

    public String getDocumento() {
        return documento;
    }
}
