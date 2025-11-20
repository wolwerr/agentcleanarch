// java
package com.poc.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.domain.exception.AiClientException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenAiHttpClientTest {

    private static void injectHttpClient(OpenAiHttpClient client, HttpClient mockHttpClient) throws Exception {
        Field f = OpenAiHttpClient.class.getDeclaredField("httpClient");
        f.setAccessible(true);
        f.set(client, mockHttpClient);
    }

    @Test
    void returnsMessageContent_whenChoicesMessagePresent_andSetsAuthorizationHeader() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OpenAiHttpClient client = new OpenAiHttpClient(mapper, "secret-key");

        HttpClient mockHttp = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String body = "{\"choices\":[{\"message\":{\"content\":\"resultado\"}}]}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(body);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        injectHttpClient(client, mockHttp);

        String result = client.sendWithRetry("{\"input\":\"x\"}");
        assertEquals("resultado", result);

        ArgumentCaptor<HttpRequest> reqCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttp).send(reqCaptor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest sent = reqCaptor.getValue();
        assertTrue(sent.headers().firstValue("Authorization").isPresent());
        assertEquals("Bearer secret-key", sent.headers().firstValue("Authorization").get());
    }

    @Test
    void returnsText_whenChoiceHasText() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OpenAiHttpClient client = new OpenAiHttpClient(mapper, "k");

        HttpClient mockHttp = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        String body = "{\"choices\":[{\"text\":\"plain text\"}]}";
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(body);
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        injectHttpClient(client, mockHttp);

        String result = client.sendWithRetry("payload");
        assertEquals("plain text", result);
    }

    @Test
    void returnsRawBody_whenMapperThrows() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.readTree(anyString())).thenThrow(new RuntimeException("parse fail"));

        OpenAiHttpClient client = new OpenAiHttpClient(mapper, "k");

        HttpClient mockHttp = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("raw-body");
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        injectHttpClient(client, mockHttp);

        String result = client.sendWithRetry("p");
        assertEquals("raw-body", result);
    }

    @Test
    void throwsAiClientException_whenNon2xxAndNotRetriable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OpenAiHttpClient client = new OpenAiHttpClient(mapper, "k");

        HttpClient mockHttp = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("bad request");
        when(mockHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        injectHttpClient(client, mockHttp);

        AiClientException ex = assertThrows(AiClientException.class, () -> client.sendWithRetry("p"));
        assertTrue(ex.getMessage().contains("OpenAI retornou status 400"));
    }
}
