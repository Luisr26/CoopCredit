package com.coopcredit.credit.domain.exception;

/**
 * Excepción base para entidades no encontradas.
 * 
 * SOLID - OCP: Abierta para extensión (subclases específicas por entidad),
 * cerrada para modificación.
 */
public abstract class EntityNotFoundException extends DomainException {

    private final Object identifier;

    protected EntityNotFoundException(String message, String entityType, Object identifier) {
        super(message, "ENTITY_NOT_FOUND", entityType);
        this.identifier = identifier;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
