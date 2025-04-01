package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.jboss.pnc.jshim.backend.tools.BasicTool;
import org.jboss.pnc.jshim.backend.tools.MavenTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HookTest {

    Path rootPath;
    Path tmpDir;

    @BeforeEach
    void before() throws IOException {
        rootPath = Files.createTempDirectory("root-path");
        tmpDir = Files.createTempDirectory("tmp-dir");
        DefaultConstants.setOverrideDataPath(tmpDir);
    }

    @AfterEach
    void after() throws IOException {
        DefaultConstants.resetOverrideDataPath();

        if (rootPath != null && Files.exists(rootPath)) {
            FileUtils.deleteDirectory(rootPath.toFile());
        }

        if (tmpDir != null && Files.exists(tmpDir)) {
            FileUtils.deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    void testRunPostDownloadHookScript() throws IOException {

        // prepare the groovy hook folder
        Path hookFolder = rootPath.resolve(DefaultConstants.HOOK_FOLDER);
        // create the folder
        Files.createDirectory(hookFolder);
        Path testGroovy = Paths.get("src/test/resources/hook-test/maven-postdownload.groovy");

        // copy the groovy script to the hook folder
        // groovy script writes the variables that were injected into /tmp/jshim-hook-test
        Files.copy(testGroovy, hookFolder.resolve("maven-postdownload.groovy"));

        BasicTool tool = new MavenTool();
        String version = "4.5.6";

        // create it just to bypass checks done in runPostDownloadHookScript
        Path versionedToolFolder = DefaultConstants.getVersionedToolFolder(tool.name(), version);
        FilesCommon.createFolderAndParent(versionedToolFolder);

        Hook.runPostDownloadHookScript(rootPath, tool, version);

        Path outputOfGroovy = Path.of("/tmp/jshim-hook-test");
        List<String> contents = Files.lines(outputOfGroovy).toList();

        assertEquals(tool.name(), contents.get(0));
        assertEquals(version, contents.get(1));
        assertEquals(versionedToolFolder.toAbsolutePath().toString(), contents.get(2));

        Files.deleteIfExists(outputOfGroovy);
    }
}