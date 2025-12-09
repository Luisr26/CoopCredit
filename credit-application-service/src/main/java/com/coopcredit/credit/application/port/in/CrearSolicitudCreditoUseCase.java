package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.CrearSolicitudRequest;
import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;

public interface CrearSolicitudCreditoUseCase {
    SolicitudCreditoDTO crear(CrearSolicitudRequest request);
}
