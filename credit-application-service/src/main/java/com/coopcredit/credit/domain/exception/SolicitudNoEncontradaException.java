package com.coopcredit.credit.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una solicitud de crédito.
 * 
 * SOLID - LSP: Puede ser sustituida por EntityNotFoundException.
 * SOLID - SRP: Solo representa el error de solicitud no encontrada.
 */
public class SolicitudNoEncontradaException extends EntityNotFoundException {

    private static final String ENTITY_TYPE = "SolicitudCredito";

    public SolicitudNoEncontradaException(Long id) {
        super("Solicitud de crédito no encontrada con ID: " + id, ENTITY_TYPE, id);
    }
}
