package org.jboss.pnc.jshim.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

class ToolTest {

    private static final int OK = CommandLine.ExitCode.OK;
    private static final int FAILED = CommandLine.ExitCode.SOFTWARE;
    private static final int USAGE = CommandLine.ExitCode.USAGE;

    @TempDir
    Path dataPath;

    @BeforeEach
    void before() throws IOException {
        DefaultConstants.setOverrideDataPath(dataPath);

        Path bin = Files.createDirectories(DefaultConstants.getVersionedToolFolder("maven", "3.1.0").resolve("bin"));
        Path mvn = Files.writeString(bin.resolve("mvn"), "#!/bin/sh\n");
        assertTrue(mvn.toFile().setExecutable(true));
    }

    @AfterEach
    void after() {
        DefaultConstants.resetOverrideDataPath();
    }

    private static int run(String... args) {
        return new CommandLine(new App()).execute(args);
    }

    @Test
    void listVersionsLocalSucceeds() {
        assertEquals(OK, run("tool", "list-versions-local", "maven"));
    }

    @Test
    void listVersionsLocalFailsForUnsupportedTool() {
        assertEquals(FAILED, run("tool", "list-versions-local", "unknown"));
    }

    @Test
    void listVersionsDownloadFailsForUnsupportedTool() {
        assertEquals(FAILED, run("tool", "list-versions-download", "unknown"));
    }

    @Test
    void downloadSucceedsIfVersionAlreadyPresent() {
        assertEquals(OK, run("tool", "download", "maven@3.1.0"));
    }

    @Test
    void downloadFailsForUnsupportedTool() {
        assertEquals(FAILED, run("tool", "download", "unknown@1.0"));
    }

    @Test
    void downloadFailsForInvalidArgument() {
        assertEquals(FAILED, run("tool", "download", "maven"));
    }

    @Test
    void useFailsForMissingVersion() {
        assertEquals(FAILED, run("tool", "use", "maven@9.9.9"));
    }

    @Test
    void useSucceedsWithoutExistingShimFolder() throws IOException {
        Path shimFolder = DefaultConstants.getShimFolder();
        assertFalse(Files.exists(shimFolder));

        assertEquals(OK, run("tool", "use", "maven@3.1.0"));
        assertEquals(
                DefaultConstants.getVersionedToolFolder("maven", "3.1.0").resolve("bin").resolve("mvn"),
                Files.readSymbolicLink(shimFolder.resolve("mvn")));
    }

    @Test
    void useFailsForUnsupportedTool() {
        assertEquals(FAILED, run("tool", "use", "unknown@1.0"));
    }

    @Test
    void useWithoutArgumentsFailsWithUsageError() {
        assertEquals(USAGE, run("tool", "use"));
    }

    @Test
    void copy() {
        Path newFolder = dataPath.resolve("copy");

        assertEquals(OK, run("tool", "copy", "--new-folder", newFolder.toString(), "maven@3.1.0"));
        assertTrue(
                Files.isRegularFile(
                        DefaultConstants.getVersionedToolFolder(newFolder, "maven", "3.1.0")
                                .resolve("bin")
                                .resolve("mvn")));
    }

    @Test
    void copyFailsIfAnyVersionIsMissing() {
        Path newFolder = dataPath.resolve("copy");

        assertEquals(FAILED, run("tool", "copy", "--new-folder", newFolder.toString(), "maven@3.1.0", "maven@9.9.9"));
        assertTrue(Files.isDirectory(DefaultConstants.getVersionedToolFolder(newFolder, "maven", "3.1.0")));
        assertFalse(Files.exists(DefaultConstants.getVersionedToolFolder(newFolder, "maven", "9.9.9")));
    }

    @Test
    void copyWithoutArgumentsFailsWithUsageError() {
        assertEquals(USAGE, run("tool", "copy", "--new-folder", dataPath.resolve("copy").toString()));
    }
}
