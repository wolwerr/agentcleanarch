package com.poc.infra.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void classHasOpenAPIDefinitionWithExpectedInfo() {
        Class<OpenApiConfig> clazz = OpenApiConfig.class;
        OpenAPIDefinition def = clazz.getAnnotation(OpenAPIDefinition.class);
        assertNotNull(def, "Classe deve ter @OpenAPIDefinition");

        Info info = def.info();
        assertNotNull(info, "Info da @OpenAPIDefinition não deve ser nula");
        assertEquals("Agent API", info.title(), "Título do OpenAPI inesperado");
        assertEquals("1.0", info.version(), "Versão do OpenAPI inesperada");
        assertEquals("API para avaliação de projetos", info.description(), "Descrição do OpenAPI inesperada");
    }

    @Test
    void classIsApplicationScoped() {
        assertTrue(OpenApiConfig.class.isAnnotationPresent(ApplicationScoped.class),
                "Classe deve ter @ApplicationScoped");
    }
}
