package com.poc.infra.adpter.web.handler;

import com.poc.infra.exception.InvalidRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class InvalidRequestExceptionMapper implements ExceptionMapper<InvalidRequestException> {

    @Override
    public Response toResponse(InvalidRequestException exception) {
        Map<String, String> payload = Map.of(
                "message", exception.getMessage()
        );
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(payload)
                .build();
    }
}
