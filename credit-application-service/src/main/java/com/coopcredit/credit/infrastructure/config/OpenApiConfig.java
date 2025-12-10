package com.coopcredit.credit.infrastructure.config;

import io.swagger.v3.oas.models.Components;
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
 * Configuración de OpenAPI/Swagger con seguridad JWT.
 */
@Configuration
public class OpenApiConfig {

    @Value("${RENDER_EXTERNAL_URL:}")
    private String renderUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("CoopCredit API")
                        .version("1.0.0")
                        .description("Sistema Integral de Solicitudes de Crédito para Cooperativas")
                        .contact(new Contact()
                                .name("CoopCredit Team")
                                .email("soporte@coopcredit.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa tu token JWT. Formato: solo el token (sin 'Bearer ')")));

        // Configurar servidor dinámicamente
        if (renderUrl != null && !renderUrl.isEmpty()) {
            openAPI.servers(List.of(
                    new Server().url(renderUrl).description("Render Production")
            ));
        }

        return openAPI;
    }
}
