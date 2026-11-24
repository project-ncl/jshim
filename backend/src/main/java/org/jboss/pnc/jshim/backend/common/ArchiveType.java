package org.jboss.pnc.jshim.backend.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

enum ArchiveType {
    ZIP,
    TAR;

    /**
     * Detect the archive type from the file contents
     *
     * @param file file to inspect
     * @return the archive type
     * @throws IOException if the file is not a zip or a (possibly compressed) tar archive
     */
    static ArchiveType detect(Path file) throws IOException {
        try (InputStream raw = new BufferedInputStream(Files.newInputStream(file))) {
            String compressor = detectCompressor(raw);

            if (compressor == null) {
                String archiver = detectArchiver(raw);

                if (ArchiveStreamFactory.ZIP.equals(archiver)) {
                    return ZIP;
                }

                if (ArchiveStreamFactory.TAR.equals(archiver)) {
                    return TAR;
                }
            } else {
                try (InputStream decompressed = new BufferedInputStream(decompress(compressor, raw, file))) {
                    String archiver = detectArchiver(decompressed);

                    if (ArchiveStreamFactory.TAR.equals(archiver)) {
                        return TAR;
                    }
                }
            }
        }

        throw new IOException("Not a zip or (compressed) tar archive: " + file);
    }

    private static InputStream decompress(String compressor, InputStream in, Path file) throws IOException {
        try {
            return new CompressorStreamFactory().createCompressorInputStream(compressor, in);
        } catch (CompressorException e) {
            throw new IOException("Cannot decompress " + compressor + " file: " + file, e);
        }
    }

    /**
     * @return the compressor name, or null if the stream is not compressed
     */
    private static String detectCompressor(InputStream in) {
        try {
            return CompressorStreamFactory.detect(in);
        } catch (CompressorException e) {
            return null;
        }
    }

    /**
     * @return the archiver name, or null if the stream is not a known archive
     */
    private static String detectArchiver(InputStream in) {
        try {
            return ArchiveStreamFactory.detect(in);
        } catch (ArchiveException e) {
            return null;
        }
    }
}
