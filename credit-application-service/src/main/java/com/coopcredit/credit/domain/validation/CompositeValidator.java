package com.coopcredit.credit.domain.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Validador compuesto que aplica múltiples validadores.
 * 
 * SOLID - OCP: Abierto para agregar nuevos validadores sin modificar el código existente.
 * SOLID - SRP: Solo se encarga de componer y ejecutar validadores.
 * 
 * @param <T> Tipo de entidad a validar
 */
public class CompositeValidator<T> implements DomainValidator<T> {

    private final List<DomainValidator<T>> validators;

    public CompositeValidator() {
        this.validators = new ArrayList<>();
    }

    public CompositeValidator(List<DomainValidator<T>> validators) {
        this.validators = new ArrayList<>(validators);
    }

    /**
     * Agrega un validador al compuesto.
     * 
     * @param validator validador a agregar
     * @return this para encadenamiento fluido
     */
    public CompositeValidator<T> addValidator(DomainValidator<T> validator) {
        this.validators.add(validator);
        return this;
    }

    @Override
    public void validate(T entity) {
        for (DomainValidator<T> validator : validators) {
            validator.validate(entity);
        }
    }
}
