package com.poc.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ReportAggregator {

    private final ObjectMapper mapper;

    @Inject
    public ReportAggregator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ArrayNode extractAchados(JsonNode node) {
        if (node == null) {
            return null;
        }
        JsonNode arr = node.path("achados");
        if (arr.isArray()) {
            ArrayNode copy = mapper.createArrayNode();
            arr.forEach(copy::add);
            return copy;
        }
        return null;
    }

    public String gerarResumoFallback(ArrayNode achados, int totalPartes) {
        int total = achados == null ? 0 : achados.size();
        return "Análise concluída em " + totalPartes
                + " partes. Foram encontrados " + total + " achados.";
    }

    public ObjectNode fallbackForUnparsable() {
        ObjectNode fallback = mapper.createObjectNode();
        ArrayNode achados = mapper.createArrayNode();

        ObjectNode a = mapper.createObjectNode();
        a.put("arquivo", "(desconhecido)");
        a.put("problema", "Resposta não pôde ser parseada como JSON.");
        a.put("sugestao", "Verificar tamanho do chunk e formato do prompt. Conteúdo bruto foi anexado.");

        achados.add(a);
        fallback.set("achados", achados);

        return fallback;
    }
}
