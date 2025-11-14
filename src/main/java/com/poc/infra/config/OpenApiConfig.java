package com.poc.infra.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "Agent API", version = "1.0", description = "API para avaliação de projetos"))
@ApplicationScoped
public class OpenApiConfig {
    // classe vazia — anotações fornecem as informações para o OpenAPI
}
