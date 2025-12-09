package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;

public interface EvaluarSolicitudUseCase {
    SolicitudCreditoDTO evaluar(Long solicitudId);
}
