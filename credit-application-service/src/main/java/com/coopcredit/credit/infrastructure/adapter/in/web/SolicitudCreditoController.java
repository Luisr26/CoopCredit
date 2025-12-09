package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.application.dto.CrearSolicitudRequest;
import com.coopcredit.credit.application.dto.SolicitudCreditoDTO;
import com.coopcredit.credit.application.port.in.ConsultarSolicitudesUseCase;
import com.coopcredit.credit.application.port.in.CrearSolicitudCreditoUseCase;
import com.coopcredit.credit.application.port.in.EvaluarSolicitudUseCase;
import com.coopcredit.credit.domain.model.EstadoSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de solicitudes de crédito.
 */
@RestController
@RequestMapping("/api/solicitudes")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Solicitudes de Crédito", description = "Endpoints para gestión y evaluación de solicitudes de crédito")
public class SolicitudCreditoController {

    private static final Logger log = LoggerFactory.getLogger(SolicitudCreditoController.class);

    private final CrearSolicitudCreditoUseCase crearSolicitudUseCase;
    private final ConsultarSolicitudesUseCase consultarSolicitudesUseCase;
    private final EvaluarSolicitudUseCase evaluarSolicitudUseCase;

    public SolicitudCreditoController(CrearSolicitudCreditoUseCase crearSolicitudUseCase,
            ConsultarSolicitudesUseCase consultarSolicitudesUseCase,
            EvaluarSolicitudUseCase evaluarSolicitudUseCase) {
        this.crearSolicitudUseCase = crearSolicitudUseCase;
        this.consultarSolicitudesUseCase = consultarSolicitudesUseCase;
        this.evaluarSolicitudUseCase = evaluarSolicitudUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AFILIADO', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Crear solicitud de crédito", description = "Crea una nueva solicitud de crédito")
    public ResponseEntity<SolicitudCreditoDTO> crear(@Valid @RequestBody CrearSolicitudRequest request) {
        log.info("POST /api/solicitudes - afiliadoId: {}, monto: {}", request.getAfiliadoId(), request.getMonto());
        SolicitudCreditoDTO solicitud = crearSolicitudUseCase.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'AFILIADO')")
    @Operation(summary = "Obtener solicitud por ID", description = "Consulta una solicitud por su ID")
    public ResponseEntity<SolicitudCreditoDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/solicitudes/{}", id);
        SolicitudCreditoDTO solicitud = consultarSolicitudesUseCase.obtenerPorId(id);
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Listar solicitudes", description = "Lista todas las solicitudes de crédito")
    public ResponseEntity<List<SolicitudCreditoDTO>> listarTodas() {
        log.info("GET /api/solicitudes");
        List<SolicitudCreditoDTO> solicitudes = consultarSolicitudesUseCase.listarTodas();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/afiliado/{afiliadoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'AFILIADO')")
    @Operation(summary = "Listar solicitudes por afiliado", description = "Lista las solicitudes de un afiliado")
    public ResponseEntity<List<SolicitudCreditoDTO>> listarPorAfiliado(@PathVariable Long afiliadoId) {
        log.info("GET /api/solicitudes/afiliado/{}", afiliadoId);
        List<SolicitudCreditoDTO> solicitudes = consultarSolicitudesUseCase.listarPorAfiliado(afiliadoId);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Listar solicitudes por estado", description = "Lista solicitudes filtradas por estado")
    public ResponseEntity<List<SolicitudCreditoDTO>> listarPorEstado(@PathVariable EstadoSolicitud estado) {
        log.info("GET /api/solicitudes/estado/{}", estado);
        List<SolicitudCreditoDTO> solicitudes = consultarSolicitudesUseCase.listarPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    @PostMapping("/{id}/evaluar")
    @PreAuthorize("hasAnyRole('ANALISTA', 'ADMIN')")
    @Operation(summary = "Evaluar solicitud", description = "Evalúa una solicitud de crédito aplicando políticas y consultando servicio externo de riesgo")
    public ResponseEntity<SolicitudCreditoDTO> evaluar(@PathVariable Long id) {
        log.info("POST /api/solicitudes/{}/evaluar", id);
        SolicitudCreditoDTO solicitud = evaluarSolicitudUseCase.evaluar(id);
        return ResponseEntity.ok(solicitud);
    }
}
