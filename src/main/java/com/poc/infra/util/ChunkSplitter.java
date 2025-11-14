package com.poc.infra.util;

import java.util.ArrayList;
import java.util.List;

public class ChunkSplitter {

    public String[] splitIntoChunks(String text, int chunkSize) {
        if (text == null) return new String[] { "" };
        String s = text;
        if (s.length() <= chunkSize) {
            return new String[] { s };
        }

        String[] linhas = s.split("\n");
        StringBuilder atual = new StringBuilder();
        List<String> partes = new ArrayList<>();

        for (String linha : linhas) {
            boolean separador = linha.startsWith("src/") || linha.startsWith("package ") || linha.startsWith("class ");
            if (separador && atual.length() >= chunkSize * 0.8) {
                partes.add(atual.toString());
                atual.setLength(0);
            }
            if (atual.length() + linha.length() + 1 > chunkSize) {
                partes.add(atual.toString());
                atual.setLength(0);
            }
            if (!atual.isEmpty()) atual.append('\n');
            atual.append(linha);
        }
        if (!atual.isEmpty()) partes.add(atual.toString());

        return partes.toArray(new String[0]);
    }
}
