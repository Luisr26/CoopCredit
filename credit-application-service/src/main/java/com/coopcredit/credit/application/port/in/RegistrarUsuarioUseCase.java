package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.AuthResponse;
import com.coopcredit.credit.application.dto.RegistroRequest;

public interface RegistrarUsuarioUseCase {
    AuthResponse registrar(RegistroRequest request);
}
