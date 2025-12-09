package com.coopcredit.credit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidad de dominio: Afiliado
 * Representa un afiliado de la cooperativa.
 */
public class Afiliado {

    private Long id;
    private String documento;
    private String nombre;
    private BigDecimal salario;
    private LocalDate fechaAfiliacion;
    private EstadoAfiliado estado;

    // Constante de negocio: antigüedad mínima en meses
    private static final int ANTIGUEDAD_MINIMA_MESES = 6;

    public Afiliado() {
    }

    public Afiliado(Long id, String documento, String nombre, BigDecimal salario,
            LocalDate fechaAfiliacion, EstadoAfiliado estado) {
        this.id = id;
        this.documento = documento;
        this.nombre = nombre;
        this.salario = salario;
        this.fechaAfiliacion = fechaAfiliacion;
        this.estado = estado;
    }

    /**
     * Verifica si el afiliado está activo.
     */
    public boolean estaActivo() {
        return this.estado == EstadoAfiliado.ACTIVO;
    }

    /**
     * Verifica si el afiliado puede recibir crédito.
     * Debe estar activo y tener la antigüedad mínima requerida.
     */
    public boolean puedeRecibirCredito() {
        return estaActivo() && tieneAntiguedadMinima();
    }

    /**
     * Verifica si el afiliado tiene la antigüedad mínima requerida.
     */
    public boolean tieneAntiguedadMinima() {
        if (fechaAfiliacion == null) {
            return false;
        }
        long mesesAfiliacion = ChronoUnit.MONTHS.between(fechaAfiliacion, LocalDate.now());
        return mesesAfiliacion >= ANTIGUEDAD_MINIMA_MESES;
    }

    /**
     * Obtiene los meses de antigüedad del afiliado.
     */
    public long getMesesAntiguedad() {
        if (fechaAfiliacion == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(fechaAfiliacion, LocalDate.now());
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDate getFechaAfiliacion() {
        return fechaAfiliacion;
    }

    public void setFechaAfiliacion(LocalDate fechaAfiliacion) {
        this.fechaAfiliacion = fechaAfiliacion;
    }

    public EstadoAfiliado getEstado() {
        return estado;
    }

    public void setEstado(EstadoAfiliado estado) {
        this.estado = estado;
    }
}
