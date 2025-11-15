package com.poc.infra.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ChunkSplitter {

    public String[] splitIntoChunks(String text, int chunkSize) {
        if (text == null || text.isBlank()) {
            return new String[0];
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize deve ser maior que zero");
        }

        if (text.length() <= chunkSize) {
            return new String[]{ text };
        }

        String[] linhas = text.split("\n");
        List<String> partes = new ArrayList<>();
        StringBuilder atual = new StringBuilder();

        for (String linha : linhas) {
            if (linha.length() > chunkSize) {
                int offset = 0;
                while (offset < linha.length()) {
                    int end = Math.min(offset + chunkSize, linha.length());
                    partes.add(linha.substring(offset, end));
                    offset = end;
                }
                continue;
            }

            if (atual.length() + linha.length() + 1 > chunkSize) {
                partes.add(atual.toString());
                atual.setLength(0);
            }

            if (!atual.isEmpty()) {
                atual.append('\n');
            }
            atual.append(linha);
        }

        if (!atual.isEmpty()) {
            partes.add(atual.toString());
        }

        return partes.toArray(new String[0]);
    }
}
