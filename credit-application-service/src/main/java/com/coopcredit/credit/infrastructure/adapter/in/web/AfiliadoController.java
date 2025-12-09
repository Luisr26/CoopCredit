package com.coopcredit.credit.infrastructure.adapter.in.web;

import com.coopcredit.credit.application.dto.AfiliadoDTO;
import com.coopcredit.credit.application.dto.CrearAfiliadoRequest;
import com.coopcredit.credit.application.port.in.ActualizarAfiliadoUseCase;
import com.coopcredit.credit.application.port.in.ConsultarAfiliadoUseCase;
import com.coopcredit.credit.application.port.in.CrearAfiliadoUseCase;
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
 * Controlador REST para gestión de afiliados.
 */
@RestController
@RequestMapping("/api/afiliados")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Afiliados", description = "Endpoints para gestión de afiliados")
public class AfiliadoController {

    private static final Logger log = LoggerFactory.getLogger(AfiliadoController.class);

    private final CrearAfiliadoUseCase crearAfiliadoUseCase;
    private final ActualizarAfiliadoUseCase actualizarAfiliadoUseCase;
    private final ConsultarAfiliadoUseCase consultarAfiliadoUseCase;

    public AfiliadoController(CrearAfiliadoUseCase crearAfiliadoUseCase,
            ActualizarAfiliadoUseCase actualizarAfiliadoUseCase,
            ConsultarAfiliadoUseCase consultarAfiliadoUseCase) {
        this.crearAfiliadoUseCase = crearAfiliadoUseCase;
        this.actualizarAfiliadoUseCase = actualizarAfiliadoUseCase;
        this.consultarAfiliadoUseCase = consultarAfiliadoUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear afiliado", description = "Crea un nuevo afiliado en el sistema")
    public ResponseEntity<AfiliadoDTO> crear(@Valid @RequestBody CrearAfiliadoRequest request) {
        log.info("POST /api/afiliados - documento: {}", request.getDocumento());
        AfiliadoDTO afiliado = crearAfiliadoUseCase.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(afiliado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA', 'AFILIADO')")
    @Operation(summary = "Obtener afiliado por ID", description = "Consulta un afiliado por su ID")
    public ResponseEntity<AfiliadoDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/afiliados/{}", id);
        AfiliadoDTO afiliado = consultarAfiliadoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(afiliado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALISTA')")
    @Operation(summary = "Listar afiliados", description = "Lista todos los afiliados")
    public ResponseEntity<List<AfiliadoDTO>> listarTodos() {
        log.info("GET /api/afiliados");
        List<AfiliadoDTO> afiliados = consultarAfiliadoUseCase.listarTodos();
        return ResponseEntity.ok(afiliados);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar afiliado", description = "Actualiza los datos de un afiliado")
    public ResponseEntity<AfiliadoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody AfiliadoDTO afiliadoDTO) {
        log.info("PUT /api/afiliados/{}", id);
        AfiliadoDTO afiliado = actualizarAfiliadoUseCase.actualizar(id, afiliadoDTO);
        return ResponseEntity.ok(afiliado);
    }
}
