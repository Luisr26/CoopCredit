package com.coopcredit.credit.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 * 
 * SOLID - SRP: Solo se encarga de configurar la documentación
 * SOLID - OCP: Fácil de extender con nuevas configuraciones
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:Credit Application Service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(externalDocs())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("CoopCredit - Credit Application Service API")
                .description("""
                        Sistema Integral de Solicitudes de Crédito con Arquitectura Hexagonal.
                        
                        Este servicio gestiona:
                        - Gestión de afiliados
                        - Solicitudes de crédito
                        - Evaluación de riesgo crediticio
                        - Integración con servicios externos
                        - Seguridad con JWT
                        
                        Características principales:
                        - Arquitectura Hexagonal (Ports & Adapters)
                        - Circuit Breaker con Resilience4j
                        - Métricas con Micrometer y Prometheus
                        - Validaciones avanzadas
                        - Manejo global de errores (RFC 7807)
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("CoopCredit Team")
                        .email("support@coopcredit.com")
                        .url("https://coopcredit.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

    private ExternalDocumentation externalDocs() {
        return new ExternalDocumentation()
                .description("Documentación completa del proyecto")
                .url("https://github.com/coopcredit/credit-application-service");
    }

    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Servidor de desarrollo local"),
                new Server()
                        .url("http://localhost:8080")
                        .description("Servidor Docker"),
                new Server()
                        .url("https://api.coopcredit.com")
                        .description("Servidor de producción")
        );
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        Autenticación JWT Bearer Token.
                        
                        Para obtener un token:
                        1. Registrarse en POST /auth/register
                        2. Iniciar sesión en POST /auth/login
                        3. Usar el token retornado con el prefijo 'Bearer '
                        
                        Ejemplo: Bearer eyJhbGciOiJIUzI1NiJ9...
                        """);
    }
}
