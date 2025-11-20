package com.poc.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportAggregatorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ReportAggregator agg = new ReportAggregator(mapper);

    @Test
    void extractAchados_nullNode_returnsNull() {
        ArrayNode res = agg.extractAchados(null);
        assertNull(res);
    }

    @Test
    void extractAchados_whenFieldIsArray_returnsCopiedArray() {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode original = mapper.createArrayNode();
        original.add(mapper.createObjectNode().put("arquivo", "a.txt"));
        original.add(mapper.createObjectNode().put("arquivo", "b.txt"));
        root.set("achados", original);

        ArrayNode copy = agg.extractAchados(root);
        assertNotNull(copy);
        assertEquals(2, copy.size());
        // garantir que é uma cópia (não a mesma instância)
        assertNotSame(original, copy);

        // modificar original não deve alterar a cópia
        original.removeAll();
        assertEquals(2, copy.size());
    }

    @Test
    void extractAchados_whenFieldNotArray_returnsNull() {
        ObjectNode root = mapper.createObjectNode();
        root.put("achados", "não é array");
        ArrayNode res = agg.extractAchados(root);
        assertNull(res);
    }

    @Test
    void gerarResumoFallback_withNullAchados_reportsZero() {
        String resumo = agg.gerarResumoFallback(null, 5);
        assertTrue(resumo.contains("5 partes"));
        assertTrue(resumo.contains("0 achados"));
    }

    @Test
    void gerarResumoFallback_withSomeAchados_reportsCount() {
        ArrayNode achados = mapper.createArrayNode();
        achados.add(mapper.createObjectNode().put("arquivo", "x"));
        achados.add(mapper.createObjectNode().put("arquivo", "y"));

        String resumo = agg.gerarResumoFallback(achados, 2);
        assertTrue(resumo.contains("2 partes"));
        assertTrue(resumo.contains("2 achados"));
    }

    @Test
    void fallbackForUnparsable_containsExpectedFields() {
        ObjectNode fallback = agg.fallbackForUnparsable();
        assertNotNull(fallback);

        JsonNode achados = fallback.path("achados");
        assertTrue(achados.isArray());
        assertEquals(1, achados.size());

        JsonNode primeiro = achados.get(0);
        assertEquals("(desconhecido)", primeiro.path("arquivo").asText());
        assertTrue(primeiro.path("problema").asText().contains("não pôde ser parseada"));
        assertTrue(primeiro.path("sugestao").asText().toLowerCase().contains("verificar"));
    }
}
