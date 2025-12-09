package com.coopcredit.credit.infrastructure.adapter.out.persistence;

import com.coopcredit.credit.application.port.out.SolicitudCreditoRepositoryPort;
import com.coopcredit.credit.domain.model.EstadoSolicitud;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.SolicitudCreditoEntity;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.repository.SolicitudCreditoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SolicitudCreditoJpaAdapter implements SolicitudCreditoRepositoryPort {

    private final SolicitudCreditoJpaRepository repository;
    private final PersistenceMapper mapper;

    public SolicitudCreditoJpaAdapter(SolicitudCreditoJpaRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SolicitudCredito guardar(SolicitudCredito solicitud) {
        SolicitudCreditoEntity entity = mapper.toEntity(solicitud);
        SolicitudCreditoEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SolicitudCredito> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SolicitudCredito> listarTodas() {
        return repository.findAllWithDetails().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudCredito> listarPorAfiliado(Long afiliadoId) {
        return repository.findByAfiliadoId(afiliadoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudCredito> listarPorEstado(EstadoSolicitud estado) {
        return repository.findByEstado(estado).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
