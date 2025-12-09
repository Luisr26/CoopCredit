package com.coopcredit.credit.infrastructure.adapter.out.persistence.mapper;

import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.EvaluacionRiesgo;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import com.coopcredit.credit.domain.model.Usuario;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.AfiliadoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.EvaluacionRiesgoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.SolicitudCreditoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA.
 */
@Component
public class PersistenceMapper {

    // Afiliado mappings
    public AfiliadoEntity toEntity(Afiliado afiliado) {
        if (afiliado == null)
            return null;

        return AfiliadoEntity.builder()
                .id(afiliado.getId())
                .documento(afiliado.getDocumento())
                .nombre(afiliado.getNombre())
                .salario(afiliado.getSalario())
                .fechaAfiliacion(afiliado.getFechaAfiliacion())
                .estado(afiliado.getEstado())
                .build();
    }

    public Afiliado toDomain(AfiliadoEntity entity) {
        if (entity == null)
            return null;

        return new Afiliado(
                entity.getId(),
                entity.getDocumento(),
                entity.getNombre(),
                entity.getSalario(),
                entity.getFechaAfiliacion(),
                entity.getEstado());
    }

    // Evalu acionRiesgo mappings
    public EvaluacionRiesgoEntity toEntity(EvaluacionRiesgo evaluacion) {
        if (evaluacion == null)
            return null;

        return EvaluacionRiesgoEntity.builder()
                .id(evaluacion.getId())
                .score(evaluacion.getScore())
                .nivelRiesgo(evaluacion.getNivelRiesgo())
                .detalleRiesgo(evaluacion.getDetalleRiesgo())
                .aprobado(evaluacion.getAprobado())
                .motivo(evaluacion.getMotivo())
                .relacionCuotaIngreso(evaluacion.getRelacionCuotaIngreso())
                .fechaEvaluacion(evaluacion.getFechaEvaluacion())
                .build();
    }

    public EvaluacionRiesgo toDomain(EvaluacionRiesgoEntity entity) {
        if (entity == null)
            return null;

        return new EvaluacionRiesgo(
                entity.getId(),
                entity.getScore(),
                entity.getNivelRiesgo(),
                entity.getDetalleRiesgo(),
                entity.getAprobado(),
                entity.getMotivo(),
                entity.getRelacionCuotaIngreso(),
                entity.getFechaEvaluacion());
    }

    // SolicitudCredito mappings
    public SolicitudCreditoEntity toEntity(SolicitudCredito solicitud) {
        if (solicitud == null)
            return null;

        return SolicitudCreditoEntity.builder()
                .id(solicitud.getId())
                .afiliado(toEntity(solicitud.getAfiliado()))
                .monto(solicitud.getMonto())
                .plazoMeses(solicitud.getPlazoMeses())
                .tasaPropuesta(solicitud.getTasaPropuesta())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .estado(solicitud.getEstado())
                .evaluacion(toEntity(solicitud.getEvaluacion()))
                .build();
    }

    public SolicitudCredito toDomain(SolicitudCreditoEntity entity) {
        if (entity == null)
            return null;

        return new SolicitudCredito(
                entity.getId(),
                toDomain(entity.getAfiliado()),
                entity.getMonto(),
                entity.getPlazoMeses(),
                entity.getTasaPropuesta(),
                entity.getFechaSolicitud(),
                entity.getEstado(),
                toDomain(entity.getEvaluacion()));
    }

    // Usuario mappings
    public UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null)
            return null;

        return UsuarioEntity.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .email(usuario.getEmail())
                .roles(usuario.getRoles())
                .afiliado(toEntity(usuario.getAfiliado()))
                .build();
    }

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null)
            return null;

        return new Usuario(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getEmail(),
                entity.getRoles(),
                toDomain(entity.getAfiliado()));
    }
}
