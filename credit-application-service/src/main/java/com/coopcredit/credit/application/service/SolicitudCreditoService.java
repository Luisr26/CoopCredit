package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.CrearSolicitudRequest;
import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.application.mapper.SolicitudCreditoMapper;
import com.coopcredit.credit.application.port.in.ConsultarSolicitudesUseCase;
import com.coopcredit.credit.application.port.in.CrearSolicitudCreditoUseCase;
import com.coopcredit.credit.application.port.out.AfiliadoRepositoryPort;
import com.coopcredit.credit.application.port.out.SolicitudCreditoRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoInactivoException;
import com.coopcredit.credit.domain.exception.AfiliadoNoEncontradoException;
import com.coopcredit.credit.domain.exception.SolicitudNoEncontradaException;
import com.coopcredit.credit.domain.model.Afiliado;
import com.coopcredit.credit.domain.model.EstadoSolicitud;
import com.coopcredit.credit.domain.model.SolicitudCredito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de solicitudes de crédito.
 */
@Service
@Transactional
public class SolicitudCreditoService implements CrearSolicitudCreditoUseCase, ConsultarSolicitudesUseCase {

    private static final Logger log = LoggerFactory.getLogger(SolicitudCreditoService.class);

    private final SolicitudCreditoRepositoryPort solicitudRepository;
    private final AfiliadoRepositoryPort afiliadoRepository;
    private final SolicitudCreditoMapper solicitudMapper;

    public SolicitudCreditoService(SolicitudCreditoRepositoryPort solicitudRepository,
            AfiliadoRepositoryPort afiliadoRepository,
            SolicitudCreditoMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.afiliadoRepository = afiliadoRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    public SolicitudCreditoDTO crear(CrearSolicitudRequest request) {
        log.info("Creando solicitud de crédito para afiliado ID: {}", request.getAfiliadoId());

        // Buscar y validar afiliado
        Afiliado afiliado = afiliadoRepository.buscarPorId(request.getAfiliadoId())
                .orElseThrow(() -> new AfiliadoNoEncontradoException(request.getAfiliadoId()));

        // Validar que el afiliado esté activo
        if (!afiliado.estaActivo()) {
            throw new AfiliadoInactivoException(afiliado.getDocumento());
        }

        // Crear solicitud
        SolicitudCredito solicitud = new SolicitudCredito();
        solicitud.setAfiliado(afiliado);
        solicitud.setMonto(request.getMonto());
        solicitud.setPlazoMeses(request.getPlazoMeses());
        solicitud.setTasaPropuesta(request.getTasaPropuesta());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        // Guardar
        SolicitudCredito solicitudGuardada = solicitudRepository.guardar(solicitud);

        log.info("Solicitud de crédito creada exitosamente con ID: {}", solicitudGuardada.getId());

        return solicitudMapper.toDTO(solicitudGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudCreditoDTO obtenerPorId(Long id) {
        log.debug("Consultando solicitud por ID: {}", id);

        SolicitudCredito solicitud = solicitudRepository.buscarPorId(id)
                .orElseThrow(() -> new SolicitudNoEncontradaException(id));

        return solicitudMapper.toDTO(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarTodas() {
        log.debug("Listando todas las solicitudes");

        return solicitudRepository.listarTodas().stream()
                .map(solicitudMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarPorAfiliado(Long afiliadoId) {
        log.debug("Listando solicitudes del afiliado ID: {}", afiliadoId);

        return solicitudRepository.listarPorAfiliado(afiliadoId).stream()
                .map(solicitudMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudCreditoDTO> listarPorEstado(EstadoSolicitud estado) {
        log.debug("Listando solicitudes con estado: {}", estado);

        return solicitudRepository.listarPorEstado(estado).stream()
                .map(solicitudMapper::toDTO)
                .collect(Collectors.toList());
    }
}
