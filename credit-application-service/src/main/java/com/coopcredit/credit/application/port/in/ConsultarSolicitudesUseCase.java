package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.domain.model.EstadoSolicitud;

import java.util.List;

public interface ConsultarSolicitudesUseCase {
    SolicitudCreditoDTO obtenerPorId(Long id);

    List<SolicitudCreditoDTO> listarTodas();

    List<SolicitudCreditoDTO> listarPorAfiliado(Long afiliadoId);

    List<SolicitudCreditoDTO> listarPorEstado(EstadoSolicitud estado);
}
