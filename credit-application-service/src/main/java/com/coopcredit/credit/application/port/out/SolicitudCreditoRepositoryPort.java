package com.coopcredit.credit.application.port.out;

import com.coopcredit.credit.domain.model.EstadoSolicitud;
import com.coopcredit.credit.domain.model.SolicitudCredito;

import java.util.List;
import java.util.Optional;

public interface SolicitudCreditoRepositoryPort {
    SolicitudCredito guardar(SolicitudCredito solicitud);

    Optional<SolicitudCredito> buscarPorId(Long id);

    List<SolicitudCredito> listarTodas();

    List<SolicitudCredito> listarPorAfiliado(Long afiliadoId);

    List<SolicitudCredito> listarPorEstado(EstadoSolicitud estado);

    void eliminar(Long id);
}
