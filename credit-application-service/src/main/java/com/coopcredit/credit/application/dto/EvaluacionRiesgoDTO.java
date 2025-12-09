package com.coopcredit.credit.application.dto;

import com.coopcredit.credit.domain.model.NivelRiesgo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionRiesgoDTO {
    private Long id;
    private Integer score;
    private NivelRiesgo nivelRiesgo;
    private String detalleRiesgo;
    private Boolean aprobado;
    private String motivo;
    private BigDecimal relacionCuotaIngreso;
    private LocalDateTime fechaEvaluacion;
}
