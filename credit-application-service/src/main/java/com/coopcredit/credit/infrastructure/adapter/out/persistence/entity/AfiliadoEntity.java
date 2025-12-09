package com.coopcredit.credit.infrastructure.adapter.out.persistence.entity;

import com.coopcredit.credit.domain.model.EstadoAfiliado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "afiliados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfiliadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 15)
    private String documento;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salario;

    @Column(name = "fecha_afiliacion", nullable = false)
    private LocalDate fechaAfiliacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAfiliado estado;
}
