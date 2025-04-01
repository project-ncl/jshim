package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.jboss.pnc.jshim.backend.tools.BasicTool;
import org.jboss.pnc.jshim.backend.tools.JavaTool;
import org.jboss.pnc.jshim.backend.tools.NodeTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HomeFolderTest {

    Path tmpDir;

    @BeforeEach
    void before() throws IOException {
        tmpDir = Files.createTempDirectory("tmp-dir-home-folder-test");
        DefaultConstants.setOverrideDataPath(tmpDir);
    }

    @AfterEach
    void after() throws IOException {
        DefaultConstants.resetOverrideDataPath();
        if (tmpDir != null && Files.exists(tmpDir)) {
            FileUtils.deleteDirectory(tmpDir.toFile());
        }
    }

    @Test
    void setEnvironmentVariableHome() throws IOException {

        // using java tool where we need to define a java home
        BasicTool javaTool = new JavaTool();
        String version = "1.2.0";

        // we create the folder structure for java version 1.2.0
        Path versionedToolFolder = DefaultConstants.getVersionedToolFolder(javaTool.name(), version);
        FilesCommon.createFolderAndParent(versionedToolFolder);

        // make sure java wants to define an environment variable
        assertTrue(javaTool.envVarHomeDefinition().isPresent());

        // we tell HomeFolder to create the symlink
        HomeFolder.setEnvironmentVariableHome(javaTool, version);

        Path shimFolder = DefaultConstants.getShimFolder();

        Path symlinkOfFolder = shimFolder
                .resolve(DefaultConstants.HOME_FOLDER_PREFIX + javaTool.envVarHomeDefinition().get());
        // was the symlink created?
        assertTrue(Files.isSymbolicLink(symlinkOfFolder));

        // is it pointing to the right location?
        assertEquals(versionedToolFolder, Files.readSymbolicLink(symlinkOfFolder));

        Path sourceInitScript = shimFolder.resolve(DefaultConstants.ENVIRONMENT_VARIABLE_FILE);

        // make sure that the sourceInitScript has the export line
        long exportLinesCount = Files.lines(sourceInitScript)
                .filter(
                        line -> line.contains(
                                "export " + javaTool.envVarHomeDefinition().get() + "="
                                        + symlinkOfFolder.toAbsolutePath().toString()))
                .count();

        assertEquals(1, exportLinesCount);
    }

    @Test
    void nothingToSetEnvironmentVariableHome() throws IOException {

        // using java tool where we need to define a java home
        BasicTool nodeTool = new NodeTool();
        String version = "1.2.0";

        // we create the folder structure for node version 1.2.0
        Path versionedToolFolder = DefaultConstants.getVersionedToolFolder(nodeTool.name(), version);
        FilesCommon.createFolderAndParent(versionedToolFolder);

        // make sure node doesn't want to define an environment variable
        assertTrue(nodeTool.envVarHomeDefinition().isEmpty());

        // we tell HomeFolder to create the symlink
        HomeFolder.setEnvironmentVariableHome(nodeTool, version);

        Path shimFolder = DefaultConstants.getShimFolder();
        // find any files/folder which start with the HOME_FOLDER_PREFIX. There should be none
        Map<String, Path> files = FilesCommon.nameOfFileAndPathFromFolder(
                shimFolder,
                path -> path.getFileName().startsWith(DefaultConstants.HOME_FOLDER_PREFIX));
        assertTrue(files.isEmpty());
    }
}