package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.AfiliadoDTO;
import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;

public interface CrearAfiliadoUseCase {
    AfiliadoDTO crear(CrearAfiliadoRequest request);
}
