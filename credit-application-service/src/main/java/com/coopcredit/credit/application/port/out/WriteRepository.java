package com.coopcredit.credit.application.port.out;

/**
 * Interfaz base para operaciones de escritura en repositorios.
 * 
 * SOLID - ISP: Interface pequeña y cohesiva para operaciones de escritura.
 * Los clientes que solo necesitan escribir no dependen de métodos de lectura.
 * 
 * @param <T> Tipo de la entidad
 * @param <ID> Tipo del identificador
 */
public interface WriteRepository<T, ID> {

    /**
     * Guarda una entidad (crear o actualizar).
     */
    T guardar(T entity);

    /**
     * Elimina una entidad por su identificador.
     */
    void eliminar(ID id);
}
