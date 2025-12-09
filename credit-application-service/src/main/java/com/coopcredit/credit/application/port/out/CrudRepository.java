package com.coopcredit.credit.application.port.out;

/**
 * Interfaz compuesta para operaciones CRUD completas.
 * 
 * SOLID - ISP: Extiende interfaces segregadas para clientes que necesitan
 * ambas operaciones de lectura y escritura.
 * 
 * @param <T> Tipo de la entidad
 * @param <ID> Tipo del identificador
 */
public interface CrudRepository<T, ID> extends ReadOnlyRepository<T, ID>, WriteRepository<T, ID> {
    // Combina lectura y escritura sin agregar métodos adicionales
}
