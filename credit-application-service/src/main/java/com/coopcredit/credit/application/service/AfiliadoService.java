package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.AfiliadoDTO;
import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;
import com.coopcredit.credit.application.mapper.AfiliadoMapper;
import com.coopcredit.credit.application.port.in.ActualizarAfiliadoUseCase;
import com.coopcredit.credit.application.port.in.ConsultarAfiliadoUseCase;
import com.coopcredit.credit.application.port.in.CrearAfiliadoUseCase;
import com.coopcredit.credit.application.port.out.AfiliadoRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoNoEncontradoException;
import com.coopcredit.credit.domain.exception.DocumentoDuplicadoException;
import com.coopcredit.credit.domain.model.Afiliado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de afiliados.
 * Implementa casos de uso relacionados con afiliados.
 */
@Service
@Transactional
public class AfiliadoService implements CrearAfiliadoUseCase, ActualizarAfiliadoUseCase, ConsultarAfiliadoUseCase {

    private static final Logger log = LoggerFactory.getLogger(AfiliadoService.class);

    private final AfiliadoRepositoryPort afiliadoRepository;
    private final AfiliadoMapper afiliadoMapper;

    public AfiliadoService(AfiliadoRepositoryPort afiliadoRepository, AfiliadoMapper afiliadoMapper) {
        this.afiliadoRepository = afiliadoRepository;
        this.afiliadoMapper = afiliadoMapper;
    }

    @Override
    public AfiliadoDTO crear(CrearAfiliadoRequest request) {
        log.info("Creando nuevo afiliado con documento: {}", request.getDocumento());

        // Validar que el documento no exista
        if (afiliadoRepository.existePorDocumento(request.getDocumento())) {
            throw new DocumentoDuplicadoException(request.getDocumento());
        }

        // Convertir request a entidad de dominio
        Afiliado afiliado = afiliadoMapper.toDomain(request);

        // Guardar
        Afiliado afiliadoGuardado = afiliadoRepository.guardar(afiliado);

        log.info("Afiliado creado exitosamente con ID: {}", afiliadoGuardado.getId());

        // Convertir a DTO y retornar
        return afiliadoMapper.toDTO(afiliadoGuardado);
    }

    @Override
    public AfiliadoDTO actualizar(Long id, AfiliadoDTO afiliadoDTO) {
        log.info("Actualizando afiliado con ID: {}", id);

        // Verificar que existe
        Afiliado afiliadoExistente = afiliadoRepository.buscarPorId(id)
                .orElseThrow(() -> new AfiliadoNoEncontradoException(id));

        // Actualizar campos permitidos (no documento, ya que es único)
        afiliadoExistente.setNombre(afiliadoDTO.getNombre());
        afiliadoExistente.setSalario(afiliadoDTO.getSalario());
        afiliadoExistente.setEstado(afiliadoDTO.getEstado());
        afiliadoExistente.setFechaAfiliacion(afiliadoDTO.getFechaAfiliacion());

        // Guardar
        Afiliado afiliadoActualizado = afiliadoRepository.guardar(afiliadoExistente);

        log.info("Afiliado actualizado exitosamente: {}", id);

        return afiliadoMapper.toDTO(afiliadoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public AfiliadoDTO obtenerPorId(Long id) {
        log.debug("Consultando afiliado por ID: {}", id);

        Afiliado afiliado = afiliadoRepository.buscarPorId(id)
                .orElseThrow(() -> new AfiliadoNoEncontradoException(id));

        return afiliadoMapper.toDTO(afiliado);
    }

    @Override
    @Transactional(readOnly = true)
    public AfiliadoDTO obtenerPorDocumento(String documento) {
        log.debug("Consultando afiliado por documento: {}", documento);

        Afiliado afiliado = afiliadoRepository.buscarPorDocumento(documento)
                .orElseThrow(
                        () -> new AfiliadoNoEncontradoException("No se encontró afiliado con documento: " + documento));

        return afiliadoMapper.toDTO(afiliado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AfiliadoDTO> listarTodos() {
        log.debug("Listando todos los afiliados");

        return afiliadoRepository.listarTodos().stream()
                .map(afiliadoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
