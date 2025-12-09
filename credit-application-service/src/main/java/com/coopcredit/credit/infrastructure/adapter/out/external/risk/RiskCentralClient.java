package com.coopcredit.credit.infrastructure.adapter.out.external.risk;

import com.coopcredit.credit.application.port.out.RiskCentralPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente HTTP para el servicio externo de evaluación de riesgo.
 * Implementa el puerto RiskCentralPort.
 */
@Component
public class RiskCentralClient implements RiskCentralPort {

    private static final Logger log = LoggerFactory.getLogger(RiskCentralClient.class);

    @Value("${coopcredit.risk-central.url}")
    private String riskCentralUrl;

    private final RestTemplate restTemplate;

    public RiskCentralClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public RiskEvaluationResponse evaluarRiesgo(String documento, BigDecimal monto, Integer plazo) {
        log.info("Llamando a servicio externo de riesgo: {}", riskCentralUrl);

        try {
            // Preparar request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("documento", documento);
            requestBody.put("monto", monto);
            requestBody.put("plazo", plazo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Llamar al servicio externo
            String url = riskCentralUrl + "/risk-evaluation";
            log.debug("POST {} - Body: {}", url, requestBody);

            RiskEvaluationResponseDTO response = restTemplate.postForObject(
                    url,
                    requestEntity,
                    RiskEvaluationResponseDTO.class);

            if (response == null) {
                throw new RuntimeException("Respuesta nula del servicio de riesgo");
            }

            log.info("Respuesta recibida - Score: {}, Nivel: {}", response.getScore(), response.getNivelRiesgo());

            // Convertir a la clase interna del puerto
            return new RiskEvaluationResponse(
                    response.getDocumento(),
                    response.getScore(),
                    response.getNivelRiesgo(),
                    response.getDetalle());

        } catch (Exception e) {
            log.error("Error al consultar servicio de riesgo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al evaluar riesgo crediticio. Servicio no disponible", e);
        }
    }

    /**
     * DTO para la respuesta del servicio externo.
     */
    private static class RiskEvaluationResponseDTO {
        private String documento;
        private Integer score;
        private String nivelRiesgo;
        private String detalle;

        public RiskEvaluationResponseDTO() {
        }

        public String getDocumento() {
            return documento;
        }

        public void setDocumento(String documento) {
            this.documento = documento;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getNivelRiesgo() {
            return nivelRiesgo;
        }

        public void setNivelRiesgo(String nivelRiesgo) {
            this.nivelRiesgo = nivelRiesgo;
        }

        public String getDetalle() {
            return detalle;
        }

        public void setDetalle(String detalle) {
            this.detalle = detalle;
        }
    }
}
