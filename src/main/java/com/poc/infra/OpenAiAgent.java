package com.poc.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poc.domain.entity.ReviewReport;
import com.poc.domain.exception.AgentAnalysisException;
import com.poc.domain.exception.AiClientException;
import com.poc.domain.gateway.AiAgentGateway;
import com.poc.domain.gateway.AiHttpClient;
import com.poc.domain.gateway.PromptBuilderGateway;
import com.poc.infra.exception.PromptBuildException;
import com.poc.infra.util.ChunkSplitter;
import com.poc.infra.util.ReportAggregator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OpenAiAgent implements AiAgentGateway {

    private final AiHttpClient httpClient;
    private final PromptBuilderGateway promptBuilder;
    private final ChunkSplitter chunkSplitter;
    private final ReportAggregator aggregator;
    private final ObjectMapper mapper;
    private final String model;
    private final int maxChunkChars;

    @Inject
    public OpenAiAgent(AiHttpClient httpClient,
                       PromptBuilderGateway promptBuilder,
                       ChunkSplitter chunkSplitter,
                       ReportAggregator aggregator,
                       ObjectMapper mapper) {

        this.httpClient = httpClient;
        this.promptBuilder = promptBuilder;
        this.chunkSplitter = chunkSplitter;
        this.aggregator = aggregator;
        this.mapper = mapper;

        this.model = "gpt-4.1-mini";
        this.maxChunkChars = 12_000;
    }

    @Override
    public ReviewReport analyzeProjectSnapshot(String projectSnapshot) throws AgentAnalysisException {

        String[] partes = chunkSplitter.splitIntoChunks(projectSnapshot, maxChunkChars);

        ArrayNode achadosAgregados = mapper.createArrayNode();

        for (int i = 0; i < partes.length; i++) {

            String system = """
                Você é um arquiteto de software especialista em Java.
                Analise o trecho do projeto a seguir com base na Arquitetura Limpa
                (entidades, casos de uso, interfaces/adapters e infraestrutura).

                Responda SOMENTE em JSON, sem texto explicativo e sem cercas de código.
                Use este formato em português:
                {
                  "achados": [
                    { "arquivo": "caminho/da/classe", "problema": "descrição sucinta do problema", "sugestao": "como melhorar ou corrigir" }
                  ]
                }
                """;

            String user = "Parte " + (i + 1) + " de " + partes.length + " do snapshot do projeto:\n" + partes[i];

            String body;
            try {
                body = promptBuilder.buildRequestBody(model, system, user);
            } catch (PromptBuildException e) {
                throw new AgentAnalysisException("Erro ao construir prompt para análise da parte " + (i + 1), e);
            }

            String content;
            try {
                content = httpClient.sendWithRetry(body);
            } catch (AiClientException e) {
                throw new AgentAnalysisException("Erro ao chamar agente de IA na parte " + (i + 1), e);
            }

            JsonNode porParte;
            try {
                porParte = mapper.readTree(content.trim());
            } catch (Exception e) {
                porParte = aggregator.fallbackForUnparsable();
            }

            ArrayNode achadosDaParte = aggregator.extractAchados(porParte);
            if (achadosDaParte != null && !achadosDaParte.isEmpty()) {
                achadosAgregados.addAll(achadosDaParte);
            }
        }

        String resumo = aggregator.gerarResumoFallback(achadosAgregados, partes.length);

        ObjectNode details = mapper.createObjectNode();
        details.put("resumo", resumo);
        details.set("achados", achadosAgregados);

        ObjectNode meta = mapper.createObjectNode();
        meta.put("partes", partes.length);
        meta.put("modelo", model);
        details.set("meta", meta);

        return new ReviewReport(details);
    }
}
