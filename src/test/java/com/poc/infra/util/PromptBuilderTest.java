package com.poc.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.infra.exception.PromptBuildException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromptBuilderTest {

    @Test
    void buildRequestBody_createsExpectedJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PromptBuilder builder = new PromptBuilder(mapper);

        String json = builder.buildRequestBody("gpt-model", "system text", "user text");
        JsonNode node = mapper.readTree(json);

        assertEquals("gpt-model", node.path("model").asText());
        assertEquals("json_object", node.path("response_format").path("type").asText());

        JsonNode messages = node.path("messages");
        assertTrue(messages.isArray());
        assertEquals(2, messages.size());

        assertEquals("system", messages.get(0).path("role").asText());
        assertEquals("system text", messages.get(0).path("content").asText());

        assertEquals("user", messages.get(1).path("role").asText());
        assertEquals("user text", messages.get(1).path("content").asText());
    }

    @Test
    void buildRequestBody_withNullUserContent_emitsNullContentNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PromptBuilder builder = new PromptBuilder(mapper);

        String json = builder.buildRequestBody("m", "s", null);
        JsonNode node = mapper.readTree(json);

        JsonNode contentNode = node.path("messages").get(1).get("content");
        assertTrue(contentNode.isNull());
    }

    @Test
    void buildRequestBody_throwsPromptBuildException_whenMapperFails_includesSnippetAndCause() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.createObjectNode()).thenThrow(new RuntimeException("boom"));

        PromptBuilder builder = new PromptBuilder(mapper);
        String longUser = "a".repeat(200);

        PromptBuildException ex = assertThrows(PromptBuildException.class,
                () -> builder.buildRequestBody("modelo-123", "sys", longUser));

        assertNotNull(ex.getCause());
        assertTrue(ex.getMessage().contains("modelo-123"));

        String expectedSnippet = longUser.substring(0, 120);
        assertTrue(ex.getMessage().contains(expectedSnippet));
    }
}
