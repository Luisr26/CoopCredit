package com.coopcredit.credit.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un afiliado.
 * 
 * SOLID - LSP: Puede ser sustituida por EntityNotFoundException en cualquier contexto.
 * SOLID - SRP: Solo se encarga de representar el error de afiliado no encontrado.
 */
public class AfiliadoNoEncontradoException extends EntityNotFoundException {

    private static final String ENTITY_TYPE = "Afiliado";

    public AfiliadoNoEncontradoException(String mensaje) {
        super(mensaje, ENTITY_TYPE, mensaje);
    }

    public AfiliadoNoEncontradoException(Long id) {
        super("Afiliado no encontrado con ID: " + id, ENTITY_TYPE, id);
    }
}
