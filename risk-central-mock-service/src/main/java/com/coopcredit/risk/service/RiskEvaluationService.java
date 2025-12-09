package com.coopcredit.risk.service;

import com.coopcredit.risk.model.NivelRiesgo;
import com.coopcredit.risk.model.RiskEvaluationRequest;
import com.coopcredit.risk.model.RiskEvaluationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio de evaluación de riesgo crediticio.
 * Genera scores determinísticos basados en el documento del solicitante.
 */
@Service
public class RiskEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RiskEvaluationService.class);
    private static final int MIN_SCORE = 300;
    private static final int MAX_SCORE = 950;

    /**
     * Evalúa el riesgo crediticio de manera determinística.
     * El mismo documento siempre generará el mismo score.
     * 
     * @param request solicitud con documento, monto y plazo
     * @return respuesta con score, nivel de riesgo y detalle
     */
    public RiskEvaluationResponse evaluateRisk(RiskEvaluationRequest request) {
        log.info("Evaluando riesgo para documento: {}, monto: {}, plazo: {}",
                request.getDocumento(), request.getMonto(), request.getPlazo());

        // Generar seed determinístico a partir del documento
        int seed = generateSeedFromDocumento(request.getDocumento());

        // Calcular score determinístico entre MIN_SCORE y MAX_SCORE
        int score = calculateScoreFromSeed(seed);

        // Clasificar nivel de riesgo
        NivelRiesgo nivelRiesgo = NivelRiesgo.fromScore(score);

        // Generar detalle descriptivo
        String detalle = generateDetalle(nivelRiesgo, score, request.getMonto(), request.getPlazo());

        RiskEvaluationResponse response = RiskEvaluationResponse.builder()
                .documento(request.getDocumento())
                .score(score)
                .nivelRiesgo(nivelRiesgo.name())
                .detalle(detalle)
                .build();

        log.info("Evaluación completada - Score: {}, Nivel: {}", score, nivelRiesgo);

        return response;
    }

    /**
     * Genera un seed numérico a partir del documento.
     * Usa el hashCode del documento para asegurar consistencia.
     */
    private int generateSeedFromDocumento(String documento) {
        // Usar hashCode del documento y aplicar módulo para obtener un valor entre 0 y
        // 999
        int hash = Math.abs(documento.hashCode());
        return hash % 1000;
    }

    /**
     * Calcula un score determinístico entre MIN_SCORE y MAX_SCORE basado en el
     * seed.
     */
    private int calculateScoreFromSeed(int seed) {
        // Normalizar el seed (0-999) al rango de scores (300-950)
        int range = MAX_SCORE - MIN_SCORE;
        int score = MIN_SCORE + ((seed * range) / 1000);
        return score;
    }

    /**
     * Genera un detalle descriptivo basado en el nivel de riesgo y otros factores.
     */
    private String generateDetalle(NivelRiesgo nivelRiesgo, int score, BigDecimal monto, Integer plazo) {
        return switch (nivelRiesgo) {
            case BAJO -> String.format(
                    "Excelente historial crediticio (Score: %d). Cliente confiable con bajo riesgo de impago. " +
                            "Aprobación recomendada para monto de $%,.2f a %d meses.",
                    score, monto, plazo);
            case MEDIO -> String.format(
                    "Historial crediticio moderado (Score: %d). Cliente con riesgo medio. " +
                            "Se recomienda evaluación adicional para monto de $%,.2f a %d meses.",
                    score, monto, plazo);
            case ALTO -> String.format(
                    "Historial crediticio deficiente (Score: %d). Alto riesgo de impago. " +
                            "No se recomienda aprobación para monto de $%,.2f a %d meses sin garantías adicionales.",
                    score, monto, plazo);
        };
    }
}
