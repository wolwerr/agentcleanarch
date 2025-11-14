package com.poc.infra.util;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public final class RepoHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RepoHelper.class);

    private RepoHelper() { }

    /** Recurso preparado para uso local (path resolvido ou clone temporário). */
    public record PreparedRepo(Path pathToUse, Path tempRepoDir, boolean deleteOnClose) implements AutoCloseable {

        /** Nome usado pelo domínio e pelos use cases. */
        public String localPath() { return pathToUse.toString(); }

        /** Compatível com try-with-resources. */
        @Override
        public void close() { cleanup(); }

        public void cleanup() {
            if (!deleteOnClose || tempRepoDir == null) return;

            try {
                Files.walkFileTree(tempRepoDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        safeDeleteWithRetries(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        clearReadonly(file);
                        safeDeleteWithRetries(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        clearReadonly(dir);
                        if (!safeDeleteWithRetries(dir)) {
                            LOG.warn("Falha ao deletar diretório após tentativas: {}", dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOG.error("Falha ao percorrer diretório temporário: {}", tempRepoDir, e);
            }
        }

        private static void clearReadonly(Path p) {
            try {
                if (Files.getFileStore(p).supportsFileAttributeView("dos")) {
                    Files.setAttribute(p, "dos:readonly", false);
                }
            } catch (Exception ignored) { }
        }

        private static boolean safeDeleteWithRetries(Path p) {
            final int maxAttempts = 5;
            final long backoffMillis = 150;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    if (Files.notExists(p)) return true;
                    if (Files.isSymbolicLink(p)) {
                        Files.deleteIfExists(p);
                        return true;
                    }
                    clearReadonly(p);
                    Files.deleteIfExists(p);
                    return true;
                } catch (DirectoryNotEmptyException dne) {
                    sleepQuiet(backoffMillis);
                } catch (IOException ioe) {
                    clearReadonly(p);
                    sleepQuiet(backoffMillis);
                }
            }
            return false;
        }

        private static void sleepQuiet(long ms) {
            try {
                TimeUnit.MILLISECONDS.sleep(ms);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Oficial, use este nome. */
    public static PreparedRepo prepareRepository(String projectPath) throws Exception {
        if (projectPath == null) throw new IllegalArgumentException("projectPath é nulo");

        boolean isUrl = projectPath.startsWith("http://") || projectPath.startsWith("https://")
                || projectPath.startsWith("git@") || projectPath.endsWith(".git");

        if (!isUrl) {
            Path local = Paths.get(projectPath);
            if (!local.isAbsolute()) {
                local = Paths.get(System.getProperty("user.dir")).resolve(local).normalize();
            }
            if (!Files.exists(local) || !Files.isDirectory(local)) {
                throw new IllegalArgumentException("Caminho local inválido ou não é um diretório: " + local);
            }
            return new PreparedRepo(local, null, false); // não deleta caminho local
        }

        Path tempRepoDir = Files.createTempDirectory("repo-");
        try (Git ignored = Git.cloneRepository()
                .setURI(projectPath)
                .setDirectory(tempRepoDir.toFile())
                .setCloneAllBranches(false)
                .setDepth(1)
                .call()) {
        } catch (Exception e) {
            LOG.error("Erro ao clonar repositório '{}'", projectPath, e);
            throw new IOException("Erro ao clonar repositório " + projectPath, e);
        }
        return new PreparedRepo(tempRepoDir, tempRepoDir, true); // apagar ao fechar
    }
}
