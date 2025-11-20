package com.poc.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poc.domain.entity.ReviewReport;
import com.poc.domain.exception.AgentAnalysisException;
import com.poc.domain.exception.AiClientException;
import com.poc.domain.gateway.AiHttpClient;
import com.poc.domain.gateway.PromptBuilderGateway;
import com.poc.infra.exception.PromptBuildException;
import com.poc.infra.util.ChunkSplitter;
import com.poc.infra.util.ReportAggregator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OpenAiAgentTest {

    @Mock
    AiHttpClient httpClient;

    @Mock
    PromptBuilderGateway promptBuilder;

    @Mock
    ChunkSplitter chunkSplitter;

    @Mock
    ReportAggregator aggregator;

    ObjectMapper mapper;
    OpenAiAgent agent;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        agent = new OpenAiAgent(httpClient, promptBuilder, chunkSplitter, aggregator, mapper);
    }

    @Test
    void analyzeProjectSnapshot_whenPromptBuilderFails_throwsAgentAnalysisException() throws Exception {
        when(chunkSplitter.splitIntoChunks(anyString(), anyInt())).thenReturn(new String[] { "parte1" });
        when(promptBuilder.buildRequestBody(anyString(), anyString(), anyString()))
                .thenThrow(new PromptBuildException("boom", new RuntimeException()));

        AgentAnalysisException ex = assertThrows(AgentAnalysisException.class,
                () -> agent.analyzeProjectSnapshot("snapshot"));

        assertTrue(ex.getMessage().contains("Erro ao construir prompt"));
        verify(promptBuilder).buildRequestBody(anyString(), anyString(), anyString());
        verifyNoInteractions(httpClient);
    }

    @Test
    void analyzeProjectSnapshot_whenHttpClientFails_throwsAgentAnalysisException() throws Exception {
        when(chunkSplitter.splitIntoChunks(anyString(), anyInt())).thenReturn(new String[] { "p" });
        when(promptBuilder.buildRequestBody(anyString(), anyString(), anyString())).thenReturn("body");
        when(httpClient.sendWithRetry(anyString())).thenThrow(new AiClientException("fail"));

        AgentAnalysisException ex = assertThrows(AgentAnalysisException.class,
                () -> agent.analyzeProjectSnapshot("snap"));

        assertTrue(ex.getMessage().contains("Erro ao chamar agente de IA"));
        verify(promptBuilder).buildRequestBody(anyString(), anyString(), anyString());
        verify(httpClient).sendWithRetry("body");
    }

    @Test
    void analyzeProjectSnapshot_whenResponseUnparsable_usesFallbackAndAggregates() throws Exception {
        when(chunkSplitter.splitIntoChunks(anyString(), anyInt())).thenReturn(new String[] { "p1" });
        when(promptBuilder.buildRequestBody(anyString(), anyString(), anyString())).thenReturn("b");
        when(httpClient.sendWithRetry("b")).thenReturn("not-a-json");

        ObjectNode fallback = mapper.createObjectNode();
        ArrayNode achados = mapper.createArrayNode();
        achados.add(mapper.createObjectNode().put("arquivo", "x"));
        fallback.set("achados", achados);

        when(aggregator.fallbackForUnparsable()).thenReturn(fallback);
        when(aggregator.extractAchados(any())).thenReturn(achados);
        when(aggregator.gerarResumoFallback(any(), anyInt())).thenReturn("resumo");

        ReviewReport report = agent.analyzeProjectSnapshot("snap");

        assertNotNull(report);
        verify(aggregator).fallbackForUnparsable();
        verify(aggregator).extractAchados(any());
        verify(aggregator).gerarResumoFallback(any(ArrayNode.class), eq(1));
    }

    @Test
    void analyzeProjectSnapshot_successfulAggregation_callsComponentsForEachPart() throws Exception {
        // duas partes para verificar múltiplas iterações
        when(chunkSplitter.splitIntoChunks(anyString(), anyInt())).thenReturn(new String[] { "p1", "p2" });
        when(promptBuilder.buildRequestBody(anyString(), anyString(), anyString())).thenReturn("b1").thenReturn("b2");
        when(httpClient.sendWithRetry("b1")).thenReturn("{\"achados\":[{\"arquivo\":\"a1\"}]}");
        when(httpClient.sendWithRetry("b2")).thenReturn("{\"achados\":[{\"arquivo\":\"a2\"}]}");

        // retornar arrays distintos por chamada de extractAchados
        ArrayNode a1 = mapper.createArrayNode().add(mapper.createObjectNode().put("arquivo", "a1"));
        ArrayNode a2 = mapper.createArrayNode().add(mapper.createObjectNode().put("arquivo", "a2"));
        when(aggregator.extractAchados(any()))
                .thenReturn(a1)
                .thenReturn(a2);

        when(aggregator.gerarResumoFallback(any(ArrayNode.class), anyInt())).thenReturn("resumo-final");

        ReviewReport report = agent.analyzeProjectSnapshot("big-snapshot");

        assertNotNull(report);
        verify(promptBuilder, times(2)).buildRequestBody(anyString(), anyString(), anyString());
        verify(httpClient, times(2)).sendWithRetry(anyString());
        // capturar o ArrayNode passado para gerarResumoFallback
        ArgumentCaptor<ArrayNode> captor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(aggregator).gerarResumoFallback(captor.capture(), eq(2));
        ArrayNode aggregated = captor.getValue();
        assertEquals(2, aggregated.size());
    }
}
