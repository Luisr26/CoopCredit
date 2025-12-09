package com.coopcredit.credit.domain.validation;

import com.coopcredit.credit.domain.exception.AfiliadoInactivoException;
import com.coopcredit.credit.domain.exception.AntiguedadInsuficienteException;
import com.coopcredit.credit.domain.model.Afiliado;
import org.springframework.stereotype.Component;

/**
 * Validador para entidades Afiliado.
 * 
 * SOLID - SRP: Solo se encarga de validar afiliados.
 * SOLID - OCP: Se pueden agregar nuevas validaciones sin modificar las existentes.
 */
@Component
public class AfiliadoValidator implements DomainValidator<Afiliado> {

    private static final int ANTIGUEDAD_MINIMA_MESES = 6;

    @Override
    public void validate(Afiliado afiliado) {
        validateActivo(afiliado);
        validateAntiguedad(afiliado);
    }

    /**
     * Valida solo que el afiliado esté activo.
     */
    public void validateActivo(Afiliado afiliado) {
        if (!afiliado.estaActivo()) {
            throw new AfiliadoInactivoException(afiliado.getDocumento());
        }
    }

    /**
     * Valida solo la antigüedad mínima.
     */
    public void validateAntiguedad(Afiliado afiliado) {
        if (!afiliado.tieneAntiguedadMinima()) {
            throw new AntiguedadInsuficienteException(
                    afiliado.getMesesAntiguedad(),
                    ANTIGUEDAD_MINIMA_MESES
            );
        }
    }

    /**
     * Valida si el afiliado puede solicitar crédito (activo + antigüedad).
     */
    public void validatePuedeRecibirCredito(Afiliado afiliado) {
        validateActivo(afiliado);
        validateAntiguedad(afiliado);
    }
}
