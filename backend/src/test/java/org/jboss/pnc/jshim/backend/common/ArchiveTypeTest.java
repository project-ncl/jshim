package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchiveTypeTest {

    private static final byte[] CONTENT = "hello".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path tempDir;

    Path file;

    @BeforeEach
    void before() {
        file = tempDir.resolve("download");
    }

    @Test
    void detectZip() throws IOException {
        try (var zip = new ZipOutputStream(Files.newOutputStream(file))) {
            zip.putNextEntry(new ZipEntry("tool/bin/run"));
            zip.write(CONTENT);
            zip.closeEntry();
        }

        assertEquals(ArchiveType.ZIP, ArchiveType.detect(file));
    }

    @Test
    void detectTar() throws IOException {
        writeTar(Files.newOutputStream(file));

        assertEquals(ArchiveType.TAR, ArchiveType.detect(file));
    }

    @Test
    void detectTarGz() throws IOException {
        writeTar(new GZIPOutputStream(Files.newOutputStream(file)));

        assertEquals(ArchiveType.TAR, ArchiveType.detect(file));
    }

    @Test
    void detectTarBz2() throws IOException {
        writeTar(new BZip2CompressorOutputStream(Files.newOutputStream(file)));

        assertEquals(ArchiveType.TAR, ArchiveType.detect(file));
    }

    @Test
    void rejectCompressedNonTar() throws IOException {
        try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(file))) {
            out.write(CONTENT);
        }

        assertThrows(IOException.class, () -> ArchiveType.detect(file));
    }

    @Test
    void rejectNonArchive() throws IOException {
        Files.writeString(file, "<html><body>404 Not Found</body></html>");

        assertThrows(IOException.class, () -> ArchiveType.detect(file));
    }

    private static void writeTar(OutputStream out) throws IOException {
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(out)) {
            TarArchiveEntry entry = new TarArchiveEntry("tool/bin/run");
            entry.setSize(CONTENT.length);
            tar.putArchiveEntry(entry);
            tar.write(CONTENT);
            tar.closeArchiveEntry();
        }
    }
}
