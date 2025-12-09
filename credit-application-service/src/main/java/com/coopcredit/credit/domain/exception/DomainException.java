package com.coopcredit.credit.domain.exception;

/**
 * Excepción base para todas las excepciones de dominio.
 * 
 * SOLID - SRP: Esta clase tiene la única responsabilidad de ser la base
 * para todas las excepciones del dominio, permitiendo un manejo uniforme.
 * 
 * SOLID - LSP: Todas las excepciones que hereden de esta clase pueden
 * ser tratadas polimórficamente.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final String entityType;

    protected DomainException(String message, String errorCode, String entityType) {
        super(message);
        this.errorCode = errorCode;
        this.entityType = entityType;
    }

    protected DomainException(String message, String errorCode, String entityType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.entityType = entityType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getEntityType() {
        return entityType;
    }
}
