package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesCommonTest {

    @TempDir
    Path tmpDir;

    @Test
    void downloadAndUnarchiveTarGz() throws Exception {
        Path sourceFolder = tmpDir.resolve("source");
        Path bin = sourceFolder.resolve("tool-1.0").resolve("bin");
        Files.createDirectories(bin);
        Files.writeString(bin.resolve("tool"), "#!/bin/sh\n");

        Path archive = tmpDir.resolve("tool-1.0.tar.gz");
        Process tar = new ProcessBuilder("tar", "czf", archive.toString(), "-C", sourceFolder.toString(), "tool-1.0")
                .start();
        assertEquals(0, tar.waitFor());

        Path target = tmpDir.resolve("target");
        FilesCommon.downloadAndUnarchive(archive.toUri().toString(), target);

        assertTrue(Files.isRegularFile(target.resolve("bin").resolve("tool")));
    }

    @Test
    void downloadAndUnarchiveFailsOnCorruptTarGz() throws IOException {
        Path archive = Files.writeString(tmpDir.resolve("corrupt.tar.gz"), "not an archive");
        Path target = tmpDir.resolve("target");

        assertThrows(IOException.class, () -> FilesCommon.downloadAndUnarchive(archive.toUri().toString(), target));
        assertFalse(Files.exists(target));
    }

    @Test
    void downloadAndUnarchiveFailsOnCorruptZip() throws IOException {
        Path archive = Files.writeString(tmpDir.resolve("corrupt.zip"), "not an archive");
        Path target = tmpDir.resolve("target");

        assertThrows(IOException.class, () -> FilesCommon.downloadAndUnarchive(archive.toUri().toString(), target));
        assertFalse(Files.exists(target));
    }

    @Test
    void downloadAndUnarchiveFailsOnMissingFile() {
        Path archive = tmpDir.resolve("missing.tar.gz");
        Path target = tmpDir.resolve("target");

        assertThrows(IOException.class, () -> FilesCommon.downloadAndUnarchive(archive.toUri().toString(), target));
        assertFalse(Files.exists(target));
    }

    @Test
    void downloadAndUnarchiveFailsOnUnsupportedArchive() throws IOException {
        Path archive = Files.writeString(tmpDir.resolve("tool.rar"), "not an archive");
        Path target = tmpDir.resolve("target");

        assertThrows(IOException.class, () -> FilesCommon.downloadAndUnarchive(archive.toUri().toString(), target));
        assertFalse(Files.exists(target));
    }

    @Test
    void downloadAndUnarchiveDoesNotTouchExistingTarget() throws IOException {
        Path archive = Files.writeString(tmpDir.resolve("corrupt.tar.gz"), "not an archive");
        Path target = tmpDir.resolve("target");
        Path existingFile = Files.createDirectories(target).resolve("existing");
        Files.writeString(existingFile, "keep me");

        assertThrows(IOException.class, () -> FilesCommon.downloadAndUnarchive(archive.toUri().toString(), target));
        assertTrue(Files.exists(existingFile));
    }
}
