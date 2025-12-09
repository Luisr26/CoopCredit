package com.coopcredit.credit.application.mapper;

import com.coopcredit.credit.application.dto.EvaluacionRiesgoDTO;
import com.coopcredit.credit.domain.model.EvaluacionRiesgo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EvaluacionRiesgoMapper {

    EvaluacionRiesgoDTO toDTO(EvaluacionRiesgo evaluacion);

    EvaluacionRiesgo toDomain(EvaluacionRiesgoDTO dto);
}
