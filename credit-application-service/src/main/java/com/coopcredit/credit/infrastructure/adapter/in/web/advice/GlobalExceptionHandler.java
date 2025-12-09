package com.coopcredit.credit.infrastructure.adapter.in.web.advice;

import com.coopcredit.credit.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manejador global de excepciones.
 * Convierte excepciones a formato ProblemDetail (RFC 7807).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AfiliadoNoEncontradoException.class)
    public ProblemDetail handleAfiliadoNoEncontrado(AfiliadoNoEncontradoException ex, WebRequest request) {
        log.warn("Afiliado no encontrado: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Afiliado no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ProblemDetail handleSolicitudNoEncontrada(SolicitudNoEncontradaException ex, WebRequest request) {
        log.warn("Solicitud no encontrada: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Solicitud no encontrada", ex.getMessage(), request);
    }

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ProblemDetail handleDocumentoDuplicado(DocumentoDuplicadoException ex, WebRequest request) {
        log.warn("Documento duplicado: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.CONFLICT, "Documento duplicado", ex.getMessage(), request);
    }

    @ExceptionHandler(AfiliadoInactivoException.class)
    public ProblemDetail handleAfiliadoInactivo(AfiliadoInactivoException ex, WebRequest request) {
        log.warn("Afiliado inactivo: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Afiliado inactivo", ex.getMessage(), request);
    }

    @ExceptionHandler(AntiguedadInsuficienteException.class)
    public ProblemDetail handleAntiguedadInsuficiente(AntiguedadInsuficienteException ex, WebRequest request) {
        log.warn("Antigüedad insuficiente: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Antigüedad insuficiente", ex.getMessage(), request);
    }

    @ExceptionHandler(CreditoRechazadoException.class)
    public ProblemDetail handleCreditoRechazado(CreditoRechazadoException ex, WebRequest request) {
        log.info("Crédito rechazado: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Crédito rechazado", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Errores de validación: {}", errors);

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Errores de validación",
                "La solicitud contiene errores de validación",
                request);
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("Credenciales inválidas");
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Credenciales inválidas",
                "Usuario o contraseña incorrectos", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tiene permisos para acceder a este recurso", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, WebRequest request) {
        log.warn("Estado ilegal: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Operación no permitida", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Argumento inválido", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Violación de integridad de datos: {}", ex.getMessage());
        String message = "Ya existe un registro con estos datos";
        if (ex.getMessage() != null && ex.getMessage().contains("username")) {
            message = "El nombre de usuario ya está registrado";
        } else if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            message = "El email ya está registrado";
        }
        return createProblemDetail(HttpStatus.CONFLICT, "Datos duplicados", message, request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Usuario no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Error de runtime: {}", ex.getMessage(), ex);
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, WebRequest request) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error inesperado",
                "Ha ocurrido un error inesperado. Por favor contacte al administrador",
                request);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("https://api.coopcredit.com/errors/" + status.value()));
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", UUID.randomUUID().toString());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return problemDetail;
    }
}
