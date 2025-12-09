package com.coopcredit.credit.application.port.out;

import com.coopcredit.credit.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorUsername(String username);

    boolean existePorEmail(String email);
}
