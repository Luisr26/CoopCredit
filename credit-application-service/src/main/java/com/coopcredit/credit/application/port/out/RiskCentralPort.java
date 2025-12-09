package com.coopcredit.credit.application.port.out;

import java.math.BigDecimal;

/**
 * Puerto de salida para comunicación con servicio externo de evaluación de
 * riesgo.
 */
public interface RiskCentralPort {

    /**
     * Evalúa el riesgo crediticio consultando el servicio externo.
     * 
     * @param documento documento del solicitante
     * @param monto     monto solicitado
     * @param plazo     plazo en meses
     * @return respuesta con score y nivel de riesgo
     */
    RiskEvaluationResponse evaluarRiesgo(String documento, BigDecimal monto, Integer plazo);

    /**
     * Clase interna para la respuesta de evaluación de riesgo.
     */
    class RiskEvaluationResponse {
        private final String documento;
        private final Integer score;
        private final String nivelRiesgo;
        private final String detalle;

        public RiskEvaluationResponse(String documento, Integer score, String nivelRiesgo, String detalle) {
            this.documento = documento;
            this.score = score;
            this.nivelRiesgo = nivelRiesgo;
            this.detalle = detalle;
        }

        public String getDocumento() {
            return documento;
        }

        public Integer getScore() {
            return score;
        }

        public String getNivelRiesgo() {
            return nivelRiesgo;
        }

        public String getDetalle() {
            return detalle;
        }
    }
}
