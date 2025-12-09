package com.coopcredit.credit.application.dto;

import com.coopcredit.credit.domain.model.EstadoAfiliado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfiliadoDTO {
    private Long id;
    private String documento;
    private String nombre;
    private BigDecimal salario;
    private LocalDate fechaAfiliacion;
    private EstadoAfiliado estado;
    private Long mesesAntiguedad;
    private Boolean puedeRecibirCredito;
}
