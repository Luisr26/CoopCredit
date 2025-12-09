package com.coopcredit.credit.application.port.in;

import com.coopcredit.credit.application.dto.AuthResponse;
import com.coopcredit.credit.application.dto.LoginRequest;

public interface AutenticarUsuarioUseCase {
    AuthResponse autenticar(LoginRequest request);
}
