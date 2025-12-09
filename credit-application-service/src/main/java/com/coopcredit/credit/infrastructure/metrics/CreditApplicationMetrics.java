package com.coopcredit.credit.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Métricas personalizadas para la aplicación de crédito.
 * 
 * SOLID - SRP: Solo se encarga de gestionar métricas
 * SOLID - OCP: Fácil agregar nuevas métricas sin modificar las existentes
 */
@Component
public class CreditApplicationMetrics {

    private final MeterRegistry meterRegistry;
    
    // Contadores
    private Counter solicitudesCreadas;
    private Counter solicitudesAprobadas;
    private Counter solicitudesRechazadas;
    private Counter errorAutenticacion;
    private Counter llamadasRiskCentral;
    private Counter fallosRiskCentral;
    
    // Timers
    private Timer tiempoEvaluacion;
    private Timer tiempoRiskCentral;
    
    // Gauges
    private final AtomicInteger solicitudesPendientes = new AtomicInteger(0);
    private final AtomicInteger afiliadosActivos = new AtomicInteger(0);

    public CreditApplicationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Inicializar contadores
        this.solicitudesCreadas = Counter.builder("credit.solicitudes.creadas")
                .description("Total de solicitudes de crédito creadas")
                .tag("type", "created")
                .register(meterRegistry);
        
        this.solicitudesAprobadas = Counter.builder("credit.solicitudes.aprobadas")
                .description("Total de solicitudes de crédito aprobadas")
                .tag("type", "approved")
                .register(meterRegistry);
        
        this.solicitudesRechazadas = Counter.builder("credit.solicitudes.rechazadas")
                .description("Total de solicitudes de crédito rechazadas")
                .tag("type", "rejected")
                .register(meterRegistry);
        
        this.errorAutenticacion = Counter.builder("credit.auth.errors")
                .description("Errores de autenticación")
                .tag("type", "authentication")
                .register(meterRegistry);
        
        this.llamadasRiskCentral = Counter.builder("credit.risk.central.calls")
                .description("Llamadas a Risk Central")
                .tag("service", "risk-central")
                .register(meterRegistry);
        
        this.fallosRiskCentral = Counter.builder("credit.risk.central.failures")
                .description("Fallos en llamadas a Risk Central")
                .tag("service", "risk-central")
                .register(meterRegistry);
        
        // Inicializar timers
        this.tiempoEvaluacion = Timer.builder("credit.evaluacion.tiempo")
                .description("Tiempo de evaluación de solicitudes")
                .tag("operation", "evaluate")
                .register(meterRegistry);
        
        this.tiempoRiskCentral = Timer.builder("credit.risk.central.tiempo")
                .description("Tiempo de respuesta de Risk Central")
                .tag("service", "risk-central")
                .register(meterRegistry);
        
        // Inicializar gauges
        Gauge.builder("credit.solicitudes.pendientes", solicitudesPendientes, AtomicInteger::get)
                .description("Número de solicitudes pendientes")
                .tag("state", "pending")
                .register(meterRegistry);
        
        Gauge.builder("credit.afiliados.activos", afiliadosActivos, AtomicInteger::get)
                .description("Número de afiliados activos")
                .tag("state", "active")
                .register(meterRegistry);
    }
    
    // Métodos para incrementar métricas
    
    public void incrementarSolicitudesCreadas() {
        solicitudesCreadas.increment();
        solicitudesPendientes.incrementAndGet();
    }
    
    public void incrementarSolicitudesAprobadas() {
        solicitudesAprobadas.increment();
        solicitudesPendientes.decrementAndGet();
    }
    
    public void incrementarSolicitudesRechazadas() {
        solicitudesRechazadas.increment();
        solicitudesPendientes.decrementAndGet();
    }
    
    public void incrementarErrorAutenticacion() {
        errorAutenticacion.increment();
    }
    
    public void incrementarLlamadasRiskCentral() {
        llamadasRiskCentral.increment();
    }
    
    public void incrementarFallosRiskCentral() {
        fallosRiskCentral.increment();
    }
    
    public void registrarTiempoEvaluacion(Runnable task) {
        tiempoEvaluacion.record(task);
    }
    
    public void registrarTiempoRiskCentral(Runnable task) {
        tiempoRiskCentral.record(task);
    }
    
    public void actualizarSolicitudesPendientes(int cantidad) {
        solicitudesPendientes.set(cantidad);
    }
    
    public void actualizarAfiliadosActivos(int cantidad) {
        afiliadosActivos.set(cantidad);
    }
    
    // Timer Sample para operaciones más complejas
    
    public Timer.Sample iniciarMedicionTiempo() {
        return Timer.start(meterRegistry);
    }
    
    public void finalizarMedicionEvaluacion(Timer.Sample sample) {
        sample.stop(tiempoEvaluacion);
    }
    
    public void finalizarMedicionRiskCentral(Timer.Sample sample) {
        sample.stop(tiempoRiskCentral);
    }
}
