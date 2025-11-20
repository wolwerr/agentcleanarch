package com.poc.infra.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RepoHelperTest {

    @Test
    void prepareRepository_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> RepoHelper.prepareRepository(null));
    }

    @Test
    void prepareRepository_invalidLocalPath_throws(@TempDir Path tmp) {
        Path notExists = tmp.resolve("no-such-dir");
        assertFalse(Files.exists(notExists));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> RepoHelper.prepareRepository(notExists.toString()));
        assertTrue(ex.getMessage().contains("Caminho local inválido"));
    }

    @Test
    void prepareRepository_withLocalDirectory_returnsPreparedRepo(@TempDir Path tmp) throws Exception {
        Path dir = Files.createDirectory(tmp.resolve("proj"));
        RepoHelper.PreparedRepo pr = RepoHelper.prepareRepository(dir.toString());

        assertEquals(dir.toString(), pr.pathToUse().toString());
        assertNull(pr.tempRepoDir());
        assertFalse(pr.deleteOnClose());
    }

    @Test
    void preparedRepo_cleanup_deletesTempDirectory(@TempDir Path tmp) throws Exception {
        Path tempRepo = Files.createDirectory(tmp.resolve("repo"));
        Path subDir = Files.createDirectory(tempRepo.resolve("sub"));
        Files.createFile(subDir.resolve("file.txt"));

        // tentar marcar como readonly quando suportado (Windows / DOS)
        try {
            if (Files.getFileStore(tempRepo).supportsFileAttributeView("dos")) {
                Files.setAttribute(subDir, "dos:readonly", true);
                Files.setAttribute(subDir.resolve("file.txt"), "dos:readonly", true);
            }
        } catch (Exception ignored) {
            // se não suportar, continua — o código de limpeza lida com isso
        }

        RepoHelper.PreparedRepo pr = new RepoHelper.PreparedRepo(tempRepo, tempRepo, true);
        pr.cleanup();

        assertFalse(Files.exists(tempRepo), "Diretório temporário deve ser removido");
    }

    @Test
    void preparedRepo_close_invokesCleanup(@TempDir Path tmp) throws Exception {
        Path tempRepo = Files.createDirectory(tmp.resolve("repoCloseTest"));
        Files.createFile(tempRepo.resolve("f.txt"));

        RepoHelper.PreparedRepo pr = new RepoHelper.PreparedRepo(tempRepo, tempRepo, true);
        pr.close();

        assertFalse(Files.exists(tempRepo), "close() deve remover o diretório temporário");
    }
}
