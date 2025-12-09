package com.coopcredit.credit.domain.validation;

/**
 * Interfaz base para validadores de dominio.
 * 
 * SOLID - ISP: Interface pequeña y cohesiva con un solo método.
 * SOLID - DIP: Las clases dependen de esta abstracción, no de implementaciones concretas.
 * 
 * @param <T> Tipo de entidad a validar
 */
@FunctionalInterface
public interface DomainValidator<T> {

    /**
     * Valida una entidad de dominio.
     * 
     * @param entity entidad a validar
     * @throws com.coopcredit.credit.domain.exception.DomainException si la validación falla
     */
    void validate(T entity);
}
