package com.poc.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.poc.domain.entity.ReviewReport;
import com.poc.domain.gateway.AiAgentGateway;
import com.poc.infra.util.ChunkSplitter;
import com.poc.infra.util.OpenAiHttpClient;
import com.poc.infra.util.PromptBuilder;
import com.poc.infra.util.ReportAggregator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.http.HttpClient;
import java.time.Duration;

@ApplicationScoped
public class OpenAiAgent implements AiAgentGateway {

    private final String apiKey;
    private final String model;
    private final int maxChunkChars;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    private final PromptBuilder promptBuilder;
    private final OpenAiHttpClient openAiHttpClient;
    private final ChunkSplitter chunkSplitter;
    private final ReportAggregator aggregator;

    @Inject
    public OpenAiAgent(
            @ConfigProperty(name = "openai.api.key") String apiKey,
            @ConfigProperty(name = "openai.model", defaultValue = "gpt-4o-mini") String model,
            @ConfigProperty(name = "openai.max-chunk-chars", defaultValue = "12000") int maxChunkChars,
            @ConfigProperty(name = "openai.max-retries", defaultValue = "3") int maxRetries
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.maxChunkChars = Math.max(2000, maxChunkChars);
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.promptBuilder = new PromptBuilder(mapper);
        this.openAiHttpClient = new OpenAiHttpClient(this.httpClient, this.apiKey, mapper, Math.max(1, maxRetries));
        this.chunkSplitter = new ChunkSplitter();
        this.aggregator = new ReportAggregator(mapper);
    }

    @Override
    public ReviewReport analyzeProjectSnapshot(String projectSnapshot) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key não configurada");
        }

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

            String body = promptBuilder.buildRequestBody(model, system, user);
            String content = openAiHttpClient.sendWithRetry(body);

            JsonNode porParte;
            try {
                porParte = mapper.readTree(content.trim());
            } catch (Exception e) {
                porParte = aggregator.fallbackForUnparsable();
            }

            ArrayNode achadosDaParte = aggregator.extractAchados(porParte);
            if (achadosDaParte != null) {
                achadosAgregados.addAll(achadosDaParte);
            }
        }

        String resumo = aggregator.gerarResumoFallback(achadosAgregados, partes.length);

        var details = mapper.createObjectNode();
        details.put("resumo", resumo);
        details.set("achados", achadosAgregados);
        var meta = mapper.createObjectNode();
        meta.put("partes", partes.length);
        meta.put("modelo", model);
        details.set("meta", meta);

        return new ReviewReport(details);
    }

    @Override
    public JsonNode completeJson(String system, String user, double temperature, int maxTokens) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key não configurada");
        }
        // Monta o request usando o PromptBuilder (reutiliza a implementação existente)
        String body = promptBuilder.buildRequestBody(model, system, user);
        // Observação: se for necessário passar temperature/maxTokens no body, ajuste PromptBuilder para suportar.
        String content = openAiHttpClient.sendWithRetry(body);

        try {
            return mapper.readTree(content.trim());
        } catch (Exception e) {
            return aggregator.fallbackForUnparsable();
        }
    }
}
