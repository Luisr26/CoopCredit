package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.AfiliadoDTO;

import java.util.List;

public interface ConsultarAfiliadoUseCase {
    AfiliadoDTO obtenerPorId(Long id);

    AfiliadoDTO obtenerPorDocumento(String documento);

    List<AfiliadoDTO> listarTodos();
}
