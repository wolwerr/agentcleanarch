package com.poc.infra.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record ReportAggregator(ObjectMapper mapper) {

    public ArrayNode extractAchados(JsonNode node) {
        if (node == null) return null;
        JsonNode arr = node.path("achados");
        if (arr.isArray()) {
            ArrayNode copy = mapper.createArrayNode();
            arr.forEach(copy::add);
            return copy;
        }
        return null;
    }

    public String gerarResumoFallback(ArrayNode achados, int partes) {
        if (achados == null || achados.isEmpty()) {
            return "Análise concluída em " + partes + " partes. " +
                    "Foram encontrados " + 0 + " achados. ";
        }

        int total = achados.size();
        StringBuilder sb = new StringBuilder();
        sb.append("Análise concluída em ").append(partes).append(" partes. ");
        sb.append("Foram encontrados ").append(total).append(" achados. ");

        int maxExemplos = Math.min(3, total);
        if (maxExemplos > 0) {
            sb.append("Exemplos: ");
            for (int i = 0; i < maxExemplos; i++) {
                JsonNode a = achados.get(i);
                String arquivo = a.path("arquivo").asText("(arquivo)");
                String problema = a.path("problema").asText("(problema)");
                sb.append("[").append(arquivo).append("] ").append(problema);
                if (i < maxExemplos - 1) sb.append("; ");
            }
            sb.append(".");
        }
        return sb.toString();
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
