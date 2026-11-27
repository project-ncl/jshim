package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChecksumTest {

    // checksums of "hello"
    private static final String HELLO_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";
    private static final String HELLO_SHA1 = "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d";
    private static final String HELLO_MD5 = "5d41402abc4b2a76b9719d911017c592";

    @TempDir
    Path tmpDir;

    @Test
    void testParseAcceptsShortAndJcaAlgorithmNames() {
        assertEquals("SHA-256", Checksum.parse("sha256:" + HELLO_SHA256).getAlgorithm());
        assertEquals("SHA-256", Checksum.parse("SHA-256:" + HELLO_SHA256).getAlgorithm());
        assertEquals("SHA-1", Checksum.parse("sha1:" + HELLO_SHA1).getAlgorithm());
        assertEquals("MD5", Checksum.parse("md5:" + HELLO_MD5).getAlgorithm());
    }

    @Test
    void testParseRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse(HELLO_SHA256));
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse(":" + HELLO_SHA256));
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse("sha256:"));
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse("sha256:not-hex"));
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse("sha256:abc"));
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse("sha256:" + HELLO_SHA1));
        assertThrows(IllegalArgumentException.class, () -> Checksum.parse("abc:" + HELLO_SHA256));
    }

    @Test
    void testVerifyMatch() throws IOException {
        Path file = Files.writeString(tmpDir.resolve("file"), "hello", StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> Checksum.parse("sha256:" + HELLO_SHA256).verify(file));
        assertDoesNotThrow(() -> Checksum.parse("sha256:" + HELLO_SHA256.toUpperCase(Locale.ROOT)).verify(file));
    }

    @Test
    void testVerifyMismatch() throws IOException {
        Path file = Files.writeString(tmpDir.resolve("file"), "hello!", StandardCharsets.UTF_8);
        Checksum checksum = Checksum.parse("sha256:" + HELLO_SHA256);

        Checksum.ChecksumMismatchException e = assertThrows(
                Checksum.ChecksumMismatchException.class,
                () -> checksum.verify(file));
        assertTrue(e.getMessage().contains(HELLO_SHA256));
    }
}
