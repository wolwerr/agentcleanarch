package com.poc.infra;

import com.poc.domain.exception.ProjectScanException;
import com.poc.domain.gateway.ProjectScannerGateway;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@ApplicationScoped
public class ProjectScanner implements ProjectScannerGateway {

    @Override
    public String scanJavaSources(Path projectPath, int maxBytes) throws ProjectScanException {
        try {
            Path root = Paths.get(projectPath.toUri());
            if (!Files.exists(root)) {
                throw new ProjectScanException("Path não encontrado: " + projectPath);
            }

            StringBuilder sb = new StringBuilder();
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path p : paths
                        .filter(Files::isRegularFile)
                        .filter(t -> t.toString().endsWith(".java"))
                        .toList()) {

                    if (sb.length() >= maxBytes) {
                        break;
                    }

                    String content = Files.readString(p, StandardCharsets.UTF_8);
                    String header = System.lineSeparator()
                            + System.lineSeparator()
                            + "// FILE: " + root.relativize(p)
                            + System.lineSeparator();

                    sb.append(header).append(content);

                    if (sb.length() > maxBytes) {
                        sb.setLength(maxBytes);
                        break;
                    }
                }
            }

            return sb.toString();
        } catch (IOException e) {
            throw new ProjectScanException("Erro ao ler arquivos do projeto em " + projectPath, e);
        }
    }
}
