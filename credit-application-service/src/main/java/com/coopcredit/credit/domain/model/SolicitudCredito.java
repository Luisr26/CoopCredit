package com.coopcredit.credit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Solicitud de Crédito
 * Representa una solicitud de crédito realizada por un afiliado.
 */
public class SolicitudCredito {

    private Long id;
    private Afiliado afiliado;
    private BigDecimal monto;
    private Integer plazoMeses;
    private BigDecimal tasaPropuesta;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private EvaluacionRiesgo evaluacion;

    public SolicitudCredito() {
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public SolicitudCredito(Long id, Afiliado afiliado, BigDecimal monto, Integer plazoMeses,
            BigDecimal tasaPropuesta, LocalDateTime fechaSolicitud,
            EstadoSolicitud estado, EvaluacionRiesgo evaluacion) {
        this.id = id;
        this.afiliado = afiliado;
        this.monto = monto;
        this.plazoMeses = plazoMeses;
        this.tasaPropuesta = tasaPropuesta;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.evaluacion = evaluacion;
    }

    /**
     * Aprueba la solicitud con la evaluación de riesgo correspondiente.
     */
    public void aprobar(EvaluacionRiesgo evaluacion) {
        this.estado = EstadoSolicitud.APROBADO;
        this.evaluacion = evaluacion;
    }

    /**
     * Rechaza la solicitud con un motivo.
     */
    public void rechazar(EvaluacionRiesgo evaluacion) {
        this.estado = EstadoSolicitud.RECHAZADO;
        this.evaluacion = evaluacion;
    }

    /**
     * Verifica si la solicitud está pendiente de evaluación.
     */
    public boolean estaPendiente() {
        return this.estado == EstadoSolicitud.PENDIENTE;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Afiliado getAfiliado() {
        return afiliado;
    }

    public void setAfiliado(Afiliado afiliado) {
        this.afiliado = afiliado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public BigDecimal getTasaPropuesta() {
        return tasaPropuesta;
    }

    public void setTasaPropuesta(BigDecimal tasaPropuesta) {
        this.tasaPropuesta = tasaPropuesta;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public EvaluacionRiesgo getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(EvaluacionRiesgo evaluacion) {
        this.evaluacion = evaluacion;
    }
}
