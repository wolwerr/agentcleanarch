package com.poc.infra;

import com.poc.domain.exception.ProjectScanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectScannerTest {

    private final ProjectScanner scanner = new ProjectScanner();

    @Test
    void scanJavaSources_nonExistingPath_throws(@TempDir Path tmp) {
        Path notExists = tmp.resolve("no-such-dir");
        ProjectScanException ex = assertThrows(ProjectScanException.class,
                () -> scanner.scanJavaSources(notExists, 10_000));
        assertTrue(ex.getMessage().contains("Path não encontrado"));
    }

    @Test
    void scanJavaSources_readsJavaFiles_andAddsHeader(@TempDir Path tmp) throws Exception {
        Path project = tmp.resolve("proj");
        Files.createDirectories(project);

        Path a = project.resolve("A.java");
        Files.writeString(a, "class A {}", StandardCharsets.UTF_8);

        Path b = project.resolve("sub").resolve("B.java");
        Files.createDirectories(b.getParent());
        Files.writeString(b, "class B {}", StandardCharsets.UTF_8);

        String result = scanner.scanJavaSources(project, 10_000);

        // contém headers e conteúdos
        assertTrue(result.contains("// FILE: " + project.relativize(a).toString()));
        assertTrue(result.contains("class A {}"));
        assertTrue(result.contains("// FILE: " + project.relativize(b).toString()));
        assertTrue(result.contains("class B {}"));
    }

    @Test
    void scanJavaSources_truncates_whenMaxBytesReached(@TempDir Path tmp) throws Exception {
        Path project = tmp.resolve("proj2");
        Files.createDirectories(project);

        // conteúdo grande para forçar truncamento
        String big = "X".repeat(200);
        Path f1 = project.resolve("One.java");
        Path f2 = project.resolve("Two.java");
        Files.writeString(f1, big, StandardCharsets.UTF_8);
        Files.writeString(f2, big, StandardCharsets.UTF_8);

        int max = 50;
        String result = scanner.scanJavaSources(project, max);

        assertEquals(max, result.length(), "Resultado deve ser truncado ao tamanho maxBytes");
        // deve conter pelo menos o header do primeiro arquivo
        assertTrue(result.contains("// FILE: "));
    }

    @Test
    void scanJavaSources_withZeroMaxBytes_returnsEmpty(@TempDir Path tmp) throws Exception {
        Path project = tmp.resolve("proj3");
        Files.createDirectories(project);
        Files.writeString(project.resolve("X.java"), "class X {}", StandardCharsets.UTF_8);

        String result = scanner.scanJavaSources(project, 0);
        assertEquals("", result);
    }
}
