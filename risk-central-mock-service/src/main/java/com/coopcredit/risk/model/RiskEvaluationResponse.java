package com.coopcredit.risk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskEvaluationResponse {

    private String documento;
    private Integer score;
    private String nivelRiesgo;
    private String detalle;
}
