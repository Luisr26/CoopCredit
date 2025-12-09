package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.AfiliadoDTO;

public interface ActualizarAfiliadoUseCase {
    AfiliadoDTO actualizar(Long id, AfiliadoDTO afiliadoDTO);
}
