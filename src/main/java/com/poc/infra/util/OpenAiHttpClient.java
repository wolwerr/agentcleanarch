package com.poc.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.domain.exception.AiClientException;
import com.poc.domain.gateway.AiHttpClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Cliente HTTP para chamadas ao endpoint OpenAI com retry simples e observabilidade via logs.
 */
@ApplicationScoped
public class OpenAiHttpClient implements AiHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiHttpClient.class);

    private static final URI ENDPOINT = URI.create("https://api.openai.com/v1/chat/completions");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final long INITIAL_BACKOFF_MS = 1500L;
    private static final long MAX_BACKOFF_MS = 8000L;
    private static final int MAX_BODY_SNIPPET = 1024;

    private final HttpClient httpClient;
    private final String apiKey;
    private final ObjectMapper mapper;
    private final int maxRetries;

    @Inject
    public OpenAiHttpClient(
            ObjectMapper mapper,
            @ConfigProperty(name = "openai.api.key", defaultValue = "") String configuredKey
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = Objects.requireNonNull(mapper, "mapper não pode ser nulo");

        String envKey = System.getenv("OPENAI_API_KEY");
        String finalKey = !configuredKey.isBlank() ? configuredKey : (envKey == null ? "" : envKey);

        if (finalKey.isBlank()) {
            throw new IllegalStateException(
                    "OpenAI API key não configurada. " +
                            "Defina 'openai.api.key' em application.properties ou a variável de ambiente OPENAI_API_KEY."
            );
        }

        this.apiKey = finalKey;
        this.maxRetries = 3;
    }

    @Override
    public String sendWithRetry(String body) throws AiClientException {
        int tentativa = 0;
        long esperaMs = INITIAL_BACKOFF_MS;

        while (true) {
            tentativa++;
            long start = System.nanoTime();
            logger.debug("Enviando requisição OpenAI, tentativa {}", tentativa);

            HttpRequest request = buildRequest(body);

            final HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                throw new AiClientException("Erro ao enviar requisição para OpenAI", e);
            }

            int status = response.statusCode();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            logger.info("OpenAI HTTP status={} tentativa={} elapsedMs={}", status, tentativa, elapsedMs);

            Optional<String> success = extractSuccessContentIf2xx(response);
            if (success.isPresent()) {
                return success.get();
            }

            if (shouldRetry(status, tentativa)) {
                logger.warn("Status {} recebido. Retry em {} ms (tentativa {}/{})",
                        status, esperaMs, tentativa, maxRetries);
                sleepWithInterruptPreserve(esperaMs);
                esperaMs = Math.min(esperaMs * 2, MAX_BACKOFF_MS);
                continue;
            }

            String bodySnippet = truncate(response.body());
            String msg = String.format("OpenAI retornou status %d, %s", status, bodySnippet);
            logger.error(msg);
            throw new AiClientException(msg);
        }
    }

    private HttpRequest buildRequest(String body) {
        return HttpRequest.newBuilder()
                .uri(ENDPOINT)
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private Optional<String> extractSuccessContentIf2xx(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status / 100 != 2) {
            return Optional.empty();
        }

        String respBody = respBodySafe(response.body());
        try {
            JsonNode respJson = mapper.readTree(respBody);
            JsonNode choices = respJson.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode choice0 = choices.get(0);
                if (choice0.has("message")) {
                    return Optional.of(choice0.path("message").path("content").asText(""));
                } else if (choice0.has("text")) {
                    return Optional.of(choice0.path("text").asText(""));
                }
            }
        } catch (Exception e) {
            logger.warn("Falha ao parsear JSON da resposta, retornando corpo bruto", e);
            return Optional.of(respBody);
        }

        logger.debug("Resposta sem campo 'choices' esperado, retornando corpo bruto.");
        return Optional.of(respBody);
    }

    private boolean shouldRetry(int status, int tentativa) {
        return (status == 429 || status / 100 == 5) && tentativa < maxRetries;
    }

    private void sleepWithInterruptPreserve(long millis) throws AiClientException {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiClientException("Thread interrompida durante backoff de retry", e);
        }
    }

    private static String respBodySafe(String body) {
        return body == null ? "" : body;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        if (s.length() <= MAX_BODY_SNIPPET) return s;
        return s.substring(0, MAX_BODY_SNIPPET) + "...(truncated)";
    }
}
