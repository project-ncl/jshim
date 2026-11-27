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

class YarnToolTest {

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
    void yarnAndYarnpkgGetShimsToYarnJs() throws IOException {
        Path home = DefaultConstants.getVersionedToolFolder("yarn", "4.19.0");
        Path bin = Files.createDirectories(home.resolve("bin"));

        for (String file : List.of("yarn", "yarn.cmd", "yarn.js")) {
            Files.createFile(
                    bin.resolve(file),
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
        }

        Map<String, Path> shims = BasicTool.shimAndSymlink(new YarnTool(), "4.19.0");

        assertEquals(Map.of("yarn", bin.resolve("yarn.js"), "yarnpkg", bin.resolve("yarn.js")), shims);
    }

    @Test
    void classicVersionsFromClassicPackageAndModernFromModernPackage() {
        List<String> classicPackageVersions = List.of("0.27.5", "1.22.22", "2.4.3");
        List<String> modernPackageVersions = List.of("2.4.1", "2.4.2", "4.19.0");

        assertEquals(
                List.of("0.27.5", "1.22.22", "2.4.1", "2.4.2", "4.19.0"),
                YarnTool.mergeVersions(classicPackageVersions, modernPackageVersions));
    }

    @Test
    void downloadUrlDependsOnMajorVersion() {
        YarnTool yarn = new YarnTool();

        assertEquals("https://registry.npmjs.org/yarn/-/yarn-1.22.22.tgz", yarn.downloadUrl("1.22.22"));
        assertEquals(
                "https://registry.npmjs.org/@yarnpkg/cli-dist/-/cli-dist-4.19.0.tgz",
                yarn.downloadUrl("4.19.0"));
    }
}
