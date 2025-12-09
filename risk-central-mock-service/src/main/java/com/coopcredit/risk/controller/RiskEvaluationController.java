package com.coopcredit.risk.controller;

import com.coopcredit.risk.model.RiskEvaluationRequest;
import com.coopcredit.risk.model.RiskEvaluationResponse;
import com.coopcredit.risk.service.RiskEvaluationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para evaluación de riesgo crediticio.
 */
@RestController
@RequestMapping("/risk-evaluation")
@CrossOrigin(origins = "*")
public class RiskEvaluationController {

    private static final Logger log = LoggerFactory.getLogger(RiskEvaluationController.class);
    private final RiskEvaluationService riskEvaluationService;

    public RiskEvaluationController(RiskEvaluationService riskEvaluationService) {
        this.riskEvaluationService = riskEvaluationService;
    }

    /**
     * Endpoint para evaluar riesgo crediticio.
     * 
     * @param request solicitud con documento, monto y plazo
     * @return evaluación de riesgo con score y nivel
     */
    @PostMapping
    public ResponseEntity<RiskEvaluationResponse> evaluateRisk(
            @Valid @RequestBody RiskEvaluationRequest request) {

        log.info("POST /risk-evaluation - Documento: {}", request.getDocumento());

        RiskEvaluationResponse response = riskEvaluationService.evaluateRisk(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Risk Central Mock Service is running");
    }
}
