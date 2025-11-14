package com.poc.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poc.infra.exception.PromptBuildException;

public record PromptBuilder(ObjectMapper mapper) {

    public String buildRequestBody(String model, String systemContent, String userContent) throws PromptBuildException {
        try {
            ObjectNode req = mapper.createObjectNode();
            req.put("model", model);
            ObjectNode responseFormat = mapper.createObjectNode().put("type", "json_object");
            req.set("response_format", responseFormat);

            var messages = req.putArray("messages");
            messages.add(mapper.createObjectNode().put("role", "system").put("content", systemContent));
            messages.add(mapper.createObjectNode().put("role", "user").put("content", userContent));

            return mapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new PromptBuildException("Falha ao construir o corpo da requisição", e);
        }
    }
}
