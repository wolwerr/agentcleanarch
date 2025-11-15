package com.poc.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poc.domain.gateway.PromptBuilderGateway;
import com.poc.infra.exception.PromptBuildException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PromptBuilder implements PromptBuilderGateway {

    private final ObjectMapper mapper;

    @Inject
    public PromptBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String buildRequestBody(String model, String systemContent, String userContent) throws PromptBuildException {
        try {
            ObjectNode req = mapper.createObjectNode();
            req.put("model", model);

            ObjectNode responseFormat = mapper.createObjectNode().put("type", "json_object");
            req.set("response_format", responseFormat);

            var messages = req.putArray("messages");
            messages.add(mapper.createObjectNode()
                    .put("role", "system")
                    .put("content", systemContent));
            messages.add(mapper.createObjectNode()
                    .put("role", "user")
                    .put("content", userContent));

            return mapper.writeValueAsString(req);
        } catch (Exception e) {
            String snippet = userContent == null
                    ? ""
                    : userContent.substring(0, Math.min(userContent.length(), 120));
            String msg = "Falha ao construir o corpo da requisição para o modelo '%s'. Trecho do userContent: '%s'"
                    .formatted(model, snippet);
            throw new PromptBuildException(msg, e);
        }
    }
}
