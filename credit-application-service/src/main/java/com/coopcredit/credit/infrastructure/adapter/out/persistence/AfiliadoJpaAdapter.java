package com.coopcredit.credit.infrastructure.adapter.out.persistence;

import com.coopcredit.credit.application.port.out.AfiliadoRepositoryPort;
import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.AfiliadoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.AfiliadoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AfiliadoJpaAdapter implements AfiliadoRepositoryPort {

    private final AfiliadoJpaRepository repository;
    private final PersistenceMapper mapper;

    public AfiliadoJpaAdapter(AfiliadoJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Afiliado guardar(Afiliado afiliado) {
        AfiliadoEntity entity = mapper.toEntity(afiliado);
        AfiliadoEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Afiliado> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Afiliado> buscarPorDocumento(String documento) {
        return repository.findByDocumento(documento).map(mapper::toDomain);
    }

    @Override
    public List<Afiliado> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return repository.existsByDocumento(documento);
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
