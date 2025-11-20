package com.poc.infra.adpter.web.handler;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GenericExceptionMapperTest {

    @Test
    void isExceptionMapperInstance() {
        GenericExceptionMapper mapper = new GenericExceptionMapper();
        assertInstanceOf(ExceptionMapper.class, mapper, "Deve implementar ExceptionMapper");
    }

    @Test
    void toResponse_returnsInternalServerErrorWithExpectedPayload() {
        GenericExceptionMapper mapper = new GenericExceptionMapper();
        Response response = mapper.toResponse(new RuntimeException("erro teste"));

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus(), "Status inesperado");

        Object entity = response.getEntity();
        assertNotNull(entity, "Entity não deve ser nulo");
        assertInstanceOf(Map.class, entity, "Entity deve ser um Map");

        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) entity;
        assertEquals("Erro interno ao processar a requisição.", payload.get("message"), "Mensagem de payload inesperada");
    }
}
