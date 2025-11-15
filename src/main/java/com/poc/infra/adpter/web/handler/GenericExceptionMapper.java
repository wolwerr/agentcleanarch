package com.poc.infra.adpter.web.handler;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Erro inesperado na API", exception);

        Map<String, String> payload = Map.of(
                "message", "Erro interno ao processar a requisição."
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(payload)
                .build();
    }
}
