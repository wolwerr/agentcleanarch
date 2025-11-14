package com.poc.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
public record OpenAiHttpClient(HttpClient httpClient, String apiKey, ObjectMapper mapper, int maxRetries) {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiHttpClient.class);

    private static final URI ENDPOINT = URI.create("https://api.openai.com/v1/chat/completions");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(90);
    private static final long INITIAL_BACKOFF_MS = 1500L;
    private static final long MAX_BACKOFF_MS = 8000L;
    private static final int MAX_BODY_SNIPPET = 1024;

    public OpenAiHttpClient(HttpClient httpClient, String apiKey, ObjectMapper mapper, int maxRetries) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient não pode ser nulo");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey não pode ser nulo");
        this.mapper = Objects.requireNonNull(mapper, "mapper não pode ser nulo");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey não pode ser vazia");
        }
        this.maxRetries = Math.max(1, maxRetries);
    }

    /**
     * Envia o corpo para o endpoint OpenAI com retry em 429 e 5xx.
     *
     * @param body corpo JSON da requisição
     * @return conteúdo de texto retornado pelo modelo ou corpo bruto quando não for possível extrair
     * @throws IOException          quando erro de IO ocorre ao enviar/ler resposta
     * @throws InterruptedException quando a thread é interrompida durante espera ou envio
     */
    public String sendWithRetry(String body) throws IOException, InterruptedException {
        int tentativa = 0;
        long esperaMs = INITIAL_BACKOFF_MS;

        while (true) {
            tentativa++;
            long start = System.nanoTime();
            logger.debug("Enviando requisição OpenAI - tentativa {}", tentativa);

            HttpRequest request = buildRequest(body);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            logger.info("OpenAI HTTP status={} tentativa={} elapsedMs={}", status, tentativa, elapsedMs);

            Optional<String> success = extractSuccessContentIf2xx(response);
            if (success.isPresent()) {
                return success.get();
            }

            if (shouldRetry(status, tentativa)) {
                logger.warn("Status {} recebido - retry em {} ms (tentativa {}/{})", status, esperaMs, tentativa, maxRetries);
                sleepWithInterruptPreserve(esperaMs);
                esperaMs = Math.min(esperaMs * 2, MAX_BACKOFF_MS);
                continue;
            }

            String bodySnippet = truncate(response.body());
            String msg = String.format("OpenAI retornou status %d - %s", status, bodySnippet);
            logger.error(msg);
            throw new IllegalStateException(msg);
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
        } catch (IOException e) {
            logger.warn("Falha ao parsear JSON da resposta (retornando corpo bruto). tentativa={}", 0, e);
            return Optional.of(respBody);
        }

        logger.debug("Resposta sem campo 'choices' esperado; retornando corpo bruto.");
        return Optional.of(respBody);
    }

    private boolean shouldRetry(int status, int tentativa) {
        return (status == 429 || status / 100 == 5) && tentativa < maxRetries;
    }

    private void sleepWithInterruptPreserve(long millis) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(millis);
    }

    private static String respBodySafe(String body) {
        return body == null ? "" : body;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        if (s.length() <= OpenAiHttpClient.MAX_BODY_SNIPPET) return s;
        return s.substring(0, OpenAiHttpClient.MAX_BODY_SNIPPET) + "...(truncated)";
    }
}
