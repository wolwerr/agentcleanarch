package com.poc.infra.adpter.web.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.RestForm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class PathRequestTest {

    @Test
    void classHasSchemaAnnotation() {
        Class<PathRequest> clazz = PathRequest.class;
        Schema schema = clazz.getAnnotation(Schema.class);
        assertNotNull(schema, "Classe deve ter @Schema");
        assertEquals("PathRequest", schema.name(), "Nome da schema inesperado");
        assertTrue(schema.description().contains("Parâmetros do formulário"),
                "Descrição da schema inesperada");
    }

    @Test
    void pathField_hasRestFormAndSchema_and_getterReturnsValue() throws Exception {
        Field pathField = PathRequest.class.getDeclaredField("path");
        assertEquals(String.class, pathField.getType(), "Tipo do campo 'path' inesperado");

        assertTrue(pathField.isAnnotationPresent(RestForm.class), "Campo 'path' deve ter @RestForm");

        Schema fieldSchema = pathField.getAnnotation(Schema.class);
        assertNotNull(fieldSchema, "Campo 'path' deve ter @Schema");
        assertTrue(fieldSchema.description().contains("URL do Github"),
                "Descrição do @Schema no campo 'path' inesperada");

        String[] examples = fieldSchema.examples();
        assertNotNull(examples, "Examples não devem ser nulos");
        assertEquals(2, examples.length, "Número de examples inesperado");
        assertEquals("https://github.com/wolwerr/order", examples[0]);
        assertEquals("C:/meus_projetos/projeto", examples[1]);

        // Verifica getter funcional
        PathRequest req = new PathRequest();
        pathField.setAccessible(true);
        pathField.set(req, "C:/meus_projetos/projeto");
        assertEquals("C:/meus_projetos/projeto", req.getPath(), "Getter getPath() retornou valor inesperado");
    }
}
