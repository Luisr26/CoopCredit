package com.coopcredit.credit.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearSolicitudRequest {

    @NotNull(message = "El ID del afiliado es obligatorio")
    private Long afiliadoId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.00", message = "El monto debe ser mayor a 0")
    @DecimalMax(value = "1000000000.00", message = "El monto excede el límite permitido")
    private BigDecimal monto;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo debe ser al menos 1 mes")
    @Max(value = 360, message = "El plazo no puede exceder 360 meses (30 años)")
    private Integer plazoMeses;

    @NotNull(message = "La tasa propuesta es obligatoria")
    @DecimalMin(value = "0.01", message = "La tasa debe ser mayor a 0")
    @DecimalMax(value = "100.00", message = "La tasa no puede exceder el 100%")
    private BigDecimal tasaPropuesta;
}
