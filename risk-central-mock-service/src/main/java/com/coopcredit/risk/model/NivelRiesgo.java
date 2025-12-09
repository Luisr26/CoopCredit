package com.coopcredit.risk.model;

public enum NivelRiesgo {
    BAJO("Bajo riesgo crediticio"),
    MEDIO("Riesgo crediticio moderado"),
    ALTO("Alto riesgo crediticio");

    private final String descripcion;

    NivelRiesgo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static NivelRiesgo fromScore(int score) {
        if (score >= 701) {
            return BAJO;
        } else if (score >= 501) {
            return MEDIO;
        } else {
            return ALTO;
        }
    }
}
