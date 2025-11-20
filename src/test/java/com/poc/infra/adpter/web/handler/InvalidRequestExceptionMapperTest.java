package com.poc.infra.adpter.web.handler;

import com.poc.infra.exception.InvalidRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InvalidRequestExceptionMapperTest {

    @Test
    void isExceptionMapperInstance() {
        InvalidRequestExceptionMapper mapper = new InvalidRequestExceptionMapper();
        assertTrue(mapper instanceof ExceptionMapper, "Deve implementar ExceptionMapper");
    }

    @Test
    void toResponse_returnsBadRequestWithExpectedPayload() {
        String message = "campo inválido";
        InvalidRequestException ex = new InvalidRequestException(message);
        InvalidRequestExceptionMapper mapper = new InvalidRequestExceptionMapper();

        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), "Status inesperado");

        Object entity = response.getEntity();
        assertNotNull(entity, "Entity não deve ser nulo");
        assertInstanceOf(Map.class, entity, "Entity deve ser um Map");

        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) entity;
        assertEquals(message, payload.get("message"), "Mensagem de payload inesperada");
    }
}
