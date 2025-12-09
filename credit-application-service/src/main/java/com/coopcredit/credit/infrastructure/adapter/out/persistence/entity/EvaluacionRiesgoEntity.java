package com.coopcredit.credit.infrastructure.adapter.out.persistence.entity;

import com.coopcredit.credit.domain.model.NivelRiesgo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluaciones_riesgo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionRiesgoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private NivelRiesgo nivelRiesgo;

    @Column(name = "detalle_riesgo", columnDefinition = "TEXT")
    private String detalleRiesgo;

    @Column(nullable = false)
    private Boolean aprobado;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "relacion_cuota_ingreso", precision = 5, scale = 4)
    private BigDecimal relacionCuotaIngreso;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDateTime fechaEvaluacion;

    @OneToOne(mappedBy = "evaluacion")
    private SolicitudCreditoEntity solicitud;
}
