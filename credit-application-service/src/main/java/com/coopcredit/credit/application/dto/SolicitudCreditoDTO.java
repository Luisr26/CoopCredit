package com.coopcredit.credit.application.dto;

import com.coopcredit.credit.domain.model.EstadoSolicitud;
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
public class SolicitudCreditoDTO {
    private Long id;
    private Long afiliadoId;
    private String afiliadoNombre;
    private String afiliadoDocumento;
    private BigDecimal monto;
    private Integer plazoMeses;
    private BigDecimal tasaPropuesta;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private EvaluacionRiesgoDTO evaluacion;
}
