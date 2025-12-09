package com.coopcredit.credit.application.dto;

import com.coopcredit.credit.domain.model.EstadoAfiliado;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearAfiliadoRequest {

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "El documento debe contener entre 6 y 15 dígitos")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El salario es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario debe ser mayor a 0")
    private BigDecimal salario;

    @NotNull(message = "La fecha de afiliación es obligatoria")
    @PastOrPresent(message = "La fecha de afiliación no puede ser futura")
    private LocalDate fechaAfiliacion;

    @NotNull(message = "El estado es obligatorio")
    private EstadoAfiliado estado;
}
