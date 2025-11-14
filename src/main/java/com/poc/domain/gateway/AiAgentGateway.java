package com.poc.domain.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.poc.domain.entity.ReviewReport;

public interface AiAgentGateway {
    ReviewReport analyzeProjectSnapshot(String projectSnapshot) throws Exception;
    /**
     * Gera uma resposta em JSON a partir do sistema (prompt de sistema) e do usuário (prompt),
     * com temperatura e limite de tokens.
     */
    JsonNode completeJson(String system, String user, double temperature, int maxTokens) throws Exception;
}