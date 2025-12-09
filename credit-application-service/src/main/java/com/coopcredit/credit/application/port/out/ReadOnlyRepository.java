package com.coopcredit.credit.application.port.out;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz base para operaciones de solo lectura en repositorios.
 * 
 * SOLID - ISP: Interface pequeña y cohesiva para operaciones de lectura.
 * Los clientes que solo necesitan leer no dependen de métodos de escritura.
 * 
 * @param <T> Tipo de la entidad
 * @param <ID> Tipo del identificador
 */
public interface ReadOnlyRepository<T, ID> {

    /**
     * Busca una entidad por su identificador.
     */
    Optional<T> buscarPorId(ID id);

    /**
     * Lista todas las entidades.
     */
    List<T> listarTodos();

    /**
     * Verifica si existe una entidad con el identificador dado.
     */
    default boolean existePorId(ID id) {
        return buscarPorId(id).isPresent();
    }
}
