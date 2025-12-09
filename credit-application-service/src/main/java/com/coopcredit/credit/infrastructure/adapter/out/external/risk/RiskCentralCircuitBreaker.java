package com.coopcredit.credit.infrastructure.adapter.out.external.risk;

import com.coopcredit.credit.application.port.out.RiskCentralPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Decorador con Circuit Breaker para el cliente de Risk Central.
 * Implementa tolerancia a fallos y fallback cuando el servicio no está disponible.
 * 
 * SOLID - SRP: Solo maneja resiliencia del servicio externo
 * SOLID - OCP: Extiende funcionalidad sin modificar RiskCentralClient
 */
@Component
@Primary
public class RiskCentralCircuitBreaker implements RiskCentralPort {

    private static final Logger log = LoggerFactory.getLogger(RiskCentralCircuitBreaker.class);

    private final RiskCentralClient riskCentralClient;
    private final Random random = new Random();

    public RiskCentralCircuitBreaker(RiskCentralClient riskCentralClient) {
        this.riskCentralClient = riskCentralClient;
    }

    @Override
    @CircuitBreaker(name = "risk-central", fallbackMethod = "evaluarRiesgoFallback")
    @Retry(name = "risk-central")
    public RiskEvaluationResponse evaluarRiesgo(String documento, BigDecimal monto, Integer plazo) {
        log.info("Llamando a Risk Central con circuit breaker - Documento: {}", documento);
        return riskCentralClient.evaluarRiesgo(documento, monto, plazo);
    }

    /**
     * Método fallback cuando el servicio no está disponible.
     * Genera una evaluación conservadora basada en reglas locales.
     */
    public RiskEvaluationResponse evaluarRiesgoFallback(String documento, BigDecimal monto, 
                                                       Integer plazo, Exception ex) {
        log.warn("Risk Central no disponible. Usando evaluación fallback. Error: {}", ex.getMessage());
        
        // Generar un score conservador basado en el monto
        int score = calcularScoreConservador(monto, plazo);
        String nivelRiesgo = determinarNivelRiesgo(score);
        
        return new RiskEvaluationResponse(
            documento,
            score,
            nivelRiesgo,
            "Evaluación offline - Servicio temporalmente no disponible. Score conservador aplicado."
        );
    }

    /**
     * Calcula un score conservador basado en reglas de negocio locales.
     */
    private int calcularScoreConservador(BigDecimal monto, Integer plazo) {
        // Regla conservadora: montos altos = scores más bajos
        BigDecimal millones = monto.divide(BigDecimal.valueOf(1000000));
        int baseScore = 600;
        
        // Reducir score por monto alto
        if (millones.compareTo(BigDecimal.valueOf(10)) > 0) {
            baseScore -= 100;
        } else if (millones.compareTo(BigDecimal.valueOf(5)) > 0) {
            baseScore -= 50;
        }
        
        // Reducir score por plazo largo
        if (plazo > 60) {
            baseScore -= 50;
        } else if (plazo > 36) {
            baseScore -= 25;
        }
        
        // Agregar variación aleatoria pequeña (±20)
        baseScore += random.nextInt(41) - 20;
        
        return Math.max(300, Math.min(850, baseScore));
    }

    private String determinarNivelRiesgo(int score) {
        if (score <= 500) {
            return "ALTO";
        } else if (score <= 700) {
            return "MEDIO";
        } else {
            return "BAJO";
        }
    }
}
