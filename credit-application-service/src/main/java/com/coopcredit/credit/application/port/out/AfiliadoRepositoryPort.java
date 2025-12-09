package com.coopcredit.credit.application.port.out;

import com.coopcredit.credit.domain.model.Afiliado;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de persistencia de Afiliados.
 * 
 * SOLID - ISP: Extiende interfaces segregadas (CRUD) y agrega operaciones específicas.
 * SOLID - DIP: Los servicios dependen de esta abstracción, no de implementaciones.
 */
public interface AfiliadoRepositoryPort extends CrudRepository<Afiliado, Long> {

    /**
     * Busca un afiliado por su documento de identidad.
     */
    Optional<Afiliado> buscarPorDocumento(String documento);

    /**
     * Verifica si existe un afiliado con el documento dado.
     */
    boolean existePorDocumento(String documento);
}
