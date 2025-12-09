package com.coopcredit.credit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object: Evaluación de Riesgo
 * Representa el resultado de la evaluación de riesgo de una solicitud de crédito.
 * 
 * SOLID - LSP: Como Value Object inmutable, cualquier instancia puede ser
 * sustituida por otra con los mismos valores sin efectos secundarios.
 * 
 * SOLID - SRP: Solo representa datos de una evaluación de riesgo.
 */
public final class EvaluacionRiesgo {

    private Long id;
    private final Integer score;
    private final NivelRiesgo nivelRiesgo;
    private final String detalleRiesgo;
    private final Boolean aprobado;
    private final String motivo;
    private final BigDecimal relacionCuotaIngreso;
    private final LocalDateTime fechaEvaluacion;

    /**
     * Constructor vacío para compatibilidad con JPA/frameworks.
     * Solo para uso interno de persistencia.
     */
    public EvaluacionRiesgo() {
        this.score = null;
        this.nivelRiesgo = null;
        this.detalleRiesgo = null;
        this.aprobado = null;
        this.motivo = null;
        this.relacionCuotaIngreso = null;
        this.fechaEvaluacion = null;
    }

    /**
     * Constructor principal para crear evaluaciones inmutables.
     */
    public EvaluacionRiesgo(Long id, Integer score, NivelRiesgo nivelRiesgo, String detalleRiesgo,
            Boolean aprobado, String motivo, BigDecimal relacionCuotaIngreso,
            LocalDateTime fechaEvaluacion) {
        this.id = id;
        this.score = score;
        this.nivelRiesgo = nivelRiesgo;
        this.detalleRiesgo = detalleRiesgo;
        this.aprobado = aprobado;
        this.motivo = motivo;
        this.relacionCuotaIngreso = relacionCuotaIngreso;
        this.fechaEvaluacion = fechaEvaluacion;
    }

    /**
     * Factory method para crear evaluación aprobada.
     */
    public static EvaluacionRiesgo aprobada(Integer score, NivelRiesgo nivelRiesgo,
            String detalleRiesgo, BigDecimal relacionCuotaIngreso) {
        return new EvaluacionRiesgo(
                null, score, nivelRiesgo, detalleRiesgo, true,
                "Solicitud aprobada. Cumple con todas las políticas de crédito.",
                relacionCuotaIngreso, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear evaluación rechazada.
     */
    public static EvaluacionRiesgo rechazada(Integer score, NivelRiesgo nivelRiesgo,
            String detalleRiesgo, String motivo, BigDecimal relacionCuotaIngreso) {
        return new EvaluacionRiesgo(
                null, score, nivelRiesgo, detalleRiesgo, false,
                motivo, relacionCuotaIngreso, LocalDateTime.now()
        );
    }

    /**
     * Crea una copia con nuevo ID (para persistencia).
     */
    public EvaluacionRiesgo withId(Long id) {
        return new EvaluacionRiesgo(
                id, this.score, this.nivelRiesgo, this.detalleRiesgo,
                this.aprobado, this.motivo, this.relacionCuotaIngreso, this.fechaEvaluacion
        );
    }

    // Solo Getters (inmutable)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public String getDetalleRiesgo() {
        return detalleRiesgo;
    }

    public Boolean getAprobado() {
        return aprobado;
    }

    public String getMotivo() {
        return motivo;
    }

    public BigDecimal getRelacionCuotaIngreso() {
        return relacionCuotaIngreso;
    }

    public LocalDateTime getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluacionRiesgo that = (EvaluacionRiesgo) o;
        return Objects.equals(score, that.score) &&
                nivelRiesgo == that.nivelRiesgo &&
                Objects.equals(aprobado, that.aprobado) &&
                Objects.equals(motivo, that.motivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, nivelRiesgo, aprobado, motivo);
    }

    @Override
    public String toString() {
        return String.format("EvaluacionRiesgo{score=%d, nivel=%s, aprobado=%s}",
                score, nivelRiesgo, aprobado);
    }
}
