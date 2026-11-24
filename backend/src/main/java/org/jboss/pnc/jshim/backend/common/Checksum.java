package org.jboss.pnc.jshim.backend.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.StandardException;

/**
 * Expected checksum of a file, given in the form {@code <algorithm>:<hex>}
 *
 * The algorithm is a {@link MessageDigest} name, e.g. {@code SHA-256}, or a short form like {@code sha256}
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Checksum {

    private final String algorithm;

    private final String hex;

    /**
     * Parse and validate a {@code <algorithm>:<hex>} string
     *
     * @throws IllegalArgumentException if the format or algorithm or hex is invalid
     */
    public static Checksum parse(String value) {
        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid checksum '" + value + "', expected <algorithm>:<hex>");
        }

        String algorithm = toJcaName(parts[0]);
        String hex = parts[1].toLowerCase(Locale.ROOT);

        int hexLength = newDigest(algorithm).getDigestLength() * 2;
        if (!hex.matches("[0-9a-f]{" + hexLength + "}")) {
            throw new IllegalArgumentException(
                    "Invalid checksum '" + value + "', " + algorithm + " must be " + hexLength + " hex characters");
        }

        return new Checksum(algorithm, hex);
    }

    /**
     * Compute the checksum of a file and compare it to the expected one
     *
     * @throws ChecksumMismatchException if the checksums are different
     */
    public void verify(Path file) throws IOException {
        String actual = hash(file);
        if (!actual.equals(hex)) {
            throw new ChecksumMismatchException(algorithm + " checksum mismatch: expected " + hex + ", got " + actual);
        }
    }

    @Override
    public String toString() {
        return algorithm + ":" + hex;
    }

    /**
     * e.g. "sha256" -> "SHA-256"
     */
    private static String toJcaName(String algorithm) {
        String upper = algorithm.toUpperCase(Locale.ROOT);
        return upper.matches("SHA\\d+") ? "SHA-" + upper.substring(3) : upper;
    }

    private static MessageDigest newDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported checksum algorithm: " + algorithm, e);
        }
    }

    private String hash(Path file) throws IOException {
        MessageDigest digest = newDigest(algorithm);
        try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
            in.transferTo(OutputStream.nullOutputStream());
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Thrown when the downloaded file doesn't match the expected checksum
     */
    @StandardException
    public static class ChecksumMismatchException extends RuntimeException {
    }
}
