package com.coopcredit.credit.application.mapper;

import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { EvaluacionRiesgoMapper.class })
public interface SolicitudCreditoMapper {

    @Mapping(source = "afiliado.id", target = "afiliadoId")
    @Mapping(source = "afiliado.nombre", target = "afiliadoNombre")
    @Mapping(source = "afiliado.documento", target = "afiliadoDocumento")
    SolicitudCreditoDTO toDTO(SolicitudCredito solicitud);

    @Mapping(target = "afiliado", ignore = true)
    SolicitudCredito toDomain(SolicitudCreditoDTO dto);
}
