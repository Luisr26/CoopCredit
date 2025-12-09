package com.coopcredit.credit.application.mapper;

import com.coopcredit.credit.application.dto.AfiliadoDTO;
import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;
import com.coopcredit.credit.domain.model.Afiliado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AfiliadoMapper {

    @Mapping(target = "mesesAntiguedad", expression = "java(afiliado.getMesesAntiguedad())")
    @Mapping(target = "puedeRecibirCredito", expression = "java(afiliado.puedeRecibirCredito())")
    AfiliadoDTO toDTO(Afiliado afiliado);

    @Mapping(target = "id", ignore = true)
    Afiliado toDomain(CrearAfiliadoRequest request);

    Afiliado toDomain(AfiliadoDTO dto);
}
