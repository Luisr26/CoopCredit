package com.coopcredit.credit.application.service;

import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.application.mapper.SolicitudCreditoMapper;
import com.coopcredit.credit.application.port.in.EvaluarSolicitudUseCase;
import com.coopcredit.credit.application.port.out.RiskCentralPort;
import com.coopcredit.credit.application.port.out.SolicitudCreditoRepositoryPort;
import com.coopcredit.credit.domain.exception.AfiliadoInactivoException;
import com.coopcredit.credit.domain.exception.AntiguedadInsuficienteException;
import com.coopcredit.credit.domain.exception.SolicitudNoEncontradaException;
import com.coopcredit.credit.domain.model.*;
import com.coopcredit.credit.infrastructure.metrics.CreditApplicationMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de aplicación para evaluar solicitudes de crédito.
 * Este es el caso de uso principal que integra la evaluación de riesgo externa
 * con las políticas de crédito internas.
 */
@Service
@Transactional
public class EvaluarSolicitudService implements EvaluarSolicitudUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluarSolicitudService.class);

    private final SolicitudCreditoRepositoryPort solicitudRepository;
    private final RiskCentralPort riskCentralPort;
    private final PoliticasCreditoService politicasService;
    private final SolicitudCreditoMapper solicitudMapper;
    private final CreditApplicationMetrics metrics;

    public EvaluarSolicitudService(SolicitudCreditoRepositoryPort solicitudRepository,
            RiskCentralPort riskCentralPort,
            PoliticasCreditoService politicasService,
            SolicitudCreditoMapper solicitudMapper,
            CreditApplicationMetrics metrics) {
        this.solicitudRepository = solicitudRepository;
        this.riskCentralPort = riskCentralPort;
        this.politicasService = politicasService;
        this.solicitudMapper = solicitudMapper;
        this.metrics = metrics;
    }

    @Override
    public SolicitudCreditoDTO evaluar(Long solicitudId) {
        log.info("Iniciando evaluación de solicitud ID: {}", solicitudId);
        
        // Iniciar medición de tiempo
        Timer.Sample sample = metrics.iniciarMedicionTiempo();

        // 1. Buscar la solicitud
        SolicitudCredito solicitud = solicitudRepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new SolicitudNoEncontradaException(solicitudId));

        // 2. Validar estado de la solicitud
        if (!solicitud.estaPendiente()) {
            throw new IllegalStateException(
                    "La solicitud ya ha sido evaluada. Estado actual: " + solicitud.getEstado());
        }

        Afiliado afiliado = solicitud.getAfiliado();

        // 3. Validar que el afiliado esté activo
        if (!afiliado.estaActivo()) {
            throw new AfiliadoInactivoException(afiliado.getDocumento());
        }

        // 4. Validar antigüedad mínima
        if (!afiliado.tieneAntiguedadMinima()) {
            throw new AntiguedadInsuficienteException(
                    afiliado.getMesesAntiguedad(),
                    politicasService.getAntiguedadMinimaMeses());
        }

        log.info("Consultando evaluación de riesgo externa para documento: {}", afiliado.getDocumento());

        // 5. Consultar servicio externo de riesgo
        RiskCentralPort.RiskEvaluationResponse riskResponse = riskCentralPort.evaluarRiesgo(
                afiliado.getDocumento(),
                solicitud.getMonto(),
                solicitud.getPlazoMeses());

        log.info("Respuesta de riesgo externo - Score: {}, Nivel: {}",
                riskResponse.getScore(), riskResponse.getNivelRiesgo());

        // 6. Aplicar políticas internas
        EvaluacionRiesgo evaluacion = aplicarPoliticasInternas(solicitud, afiliado, riskResponse);

        // 7. Actualizar estado de la solicitud según evaluación
        if (evaluacion.getAprobado()) {
            solicitud.aprobar(evaluacion);
            metrics.incrementarSolicitudesAprobadas();
            log.info("Solicitud APROBADA - ID: {}", solicitudId);
        } else {
            solicitud.rechazar(evaluacion);
            metrics.incrementarSolicitudesRechazadas();
            log.info("Solicitud RECHAZADA - ID: {}, Motivo: {}", solicitudId, evaluacion.getMotivo());
        }

        // 8. Guardar solicitud actualizada (con evaluación)
        SolicitudCredito solicitudActualizada = solicitudRepository.guardar(solicitud);

        log.info("Evaluación completada para solicitud ID: {}", solicitudId);
        
        // Finalizar medición de tiempo
        metrics.finalizarMedicionEvaluacion(sample);

        return solicitudMapper.toDTO(solicitudActualizada);
    }

    /**
     * Aplica las políticas de crédito internas y genera la evaluación final.
     */
    private EvaluacionRiesgo aplicarPoliticasInternas(SolicitudCredito solicitud,
            Afiliado afiliado,
            RiskCentralPort.RiskEvaluationResponse riskResponse) {

        List<String> motivosRechazo = new ArrayList<>();
        boolean aprobado = true;

        // Calcular cuota mensual
        BigDecimal cuotaMensual = politicasService.calcularCuotaMensual(
                solicitud.getMonto(),
                solicitud.getTasaPropuesta(),
                solicitud.getPlazoMeses());

        // Calcular relación cuota/ingreso
        BigDecimal relacionCuotaIngreso = politicasService.calcularRelacionCuotaIngreso(
                cuotaMensual,
                afiliado.getSalario());

        log.debug("Cuota mensual calculada: {}, Relación cuota/ingreso: {}",
                cuotaMensual, relacionCuotaIngreso);

        // Política 1: Relación cuota/ingreso
        if (!politicasService.cumpleRelacionCuotaIngreso(relacionCuotaIngreso)) {
            aprobado = false;
            motivosRechazo.add(String.format(
                    "Relación cuota/ingreso excede el máximo permitido (%.2f%% > %.2f%%)",
                    relacionCuotaIngreso.multiply(BigDecimal.valueOf(100)),
                    politicasService.getRelacionCuotaIngresoMaxima().multiply(BigDecimal.valueOf(100))));
        }

        // Política 2: Monto máximo según salario
        if (!politicasService.cumpleMontoMaximo(solicitud.getMonto(), afiliado.getSalario())) {
            aprobado = false;
            BigDecimal montoMaximo = afiliado.getSalario()
                    .multiply(BigDecimal.valueOf(politicasService.getMultiplicadorSalarioMontoMaximo()));
            motivosRechazo.add(String.format(
                    "El monto solicitado excede el máximo según salario ($%,.2f > $%,.2f)",
                    solicitud.getMonto(),
                    montoMaximo));
        }

        // Política 3: Score mínimo (ejemplo: rechazar si es ALTO riesgo)
        NivelRiesgo nivelRiesgo = NivelRiesgo.valueOf(riskResponse.getNivelRiesgo());
        if (nivelRiesgo == NivelRiesgo.ALTO) {
            aprobado = false;
            motivosRechazo.add("Score de riesgo crediticio ALTO (" + riskResponse.getScore() + "). " +
                    "No cumple con el perfil de riesgo aceptable.");
        }

        // Crear evaluación de riesgo usando factory methods (SOLID - LSP: Value Object inmutable)
        EvaluacionRiesgo evaluacion;
        if (aprobado) {
            evaluacion = EvaluacionRiesgo.aprobada(
                    riskResponse.getScore(),
                    nivelRiesgo,
                    riskResponse.getDetalle(),
                    relacionCuotaIngreso
            );
        } else {
            evaluacion = EvaluacionRiesgo.rechazada(
                    riskResponse.getScore(),
                    nivelRiesgo,
                    riskResponse.getDetalle(),
                    String.join(" | ", motivosRechazo),
                    relacionCuotaIngreso
            );
        }

        return evaluacion;
    }
}
