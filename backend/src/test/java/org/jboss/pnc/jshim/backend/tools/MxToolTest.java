package org.jboss.pnc.jshim.backend.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;

import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MxToolTest {

    @TempDir
    Path dataPath;

    @BeforeEach
    void setDataPath() {
        DefaultConstants.setOverrideDataPath(dataPath);
    }

    @AfterEach
    void resetDataPath() {
        DefaultConstants.resetOverrideDataPath();
    }

    @Test
    void onlyMxGetsAShim() throws IOException {
        Path home = Files.createDirectories(DefaultConstants.getVersionedToolFolder("mx", "7.89.0"));

        for (String file : List.of("mx", "mx.py", "remove_jdks.py")) {
            Files.createFile(
                    home.resolve(file),
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
        }

        assertEquals(Map.of("mx", home.resolve("mx")), BasicTool.shimAndSymlink(new MxTool(), "7.89.0"));
    }

    @Test
    void releaseVersionsAreFilteredAndSorted() {
        List<String> tags = List.of(
                "refs/tags/7.10.0",
                "refs/tags/7.9.1",
                "refs/tags/5.46.3",
                "refs/tags/jdk-21+35",
                "refs/tags/jdk-17.0.1+12",
                "refs/tags/5.316.15.1",
                "refs/tags/checkstyle-8.36");

        assertEquals(List.of("5.46.3", "7.9.1", "7.10.0"), MxTool.releaseVersionsFromTags(tags));
    }

    @Test
    void downloadUrl() {
        assertEquals(
                "https://github.com/graalvm/mx/archive/refs/tags/7.89.0.tar.gz",
                new MxTool().downloadUrl("7.89.0"));
    }
}
