package com.coopcredit.credit.infrastructure.adapter.out.persistence;

import com.coopcredit.credit.application.port.out.UsuarioRepositoryPort;
import com.coopcredit.credit.domain.model.Usuario;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioJpaAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;
    private final PersistenceMapper mapper;

    public UsuarioJpaAdapter(UsuarioJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existePorUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existePorEmail(String email) {
        return repository.existsByEmail(email);
    }
}
