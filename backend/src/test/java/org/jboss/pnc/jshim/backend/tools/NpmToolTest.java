package org.jboss.pnc.jshim.backend.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;

import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NpmToolTest {

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
    void onlyNpmAndNpxGetShims() throws IOException {
        Path home = fakePackage("10.9.0", "npm", "npx", "npm.cmd", "npm-cli.js", "npx-cli.js", "npm-prefix.js");

        Map<String, Path> shims = BasicTool.shimAndSymlink(new NpmTool(), "10.9.0");

        assertEquals(Set.of("npm", "npx"), shims.keySet());
        assertEquals(home.resolve("bin/npm-cli.js"), shims.get("npm"));
        assertEquals(home.resolve("bin/npx-cli.js"), shims.get("npx"));
    }

    @Test
    void skipsNpxWhenMissing() throws IOException {
        fakePackage("5.1.0", "npm", "npm-cli.js");

        Map<String, Path> shims = BasicTool.shimAndSymlink(new NpmTool(), "5.1.0");

        assertEquals(Set.of("npm"), shims.keySet());
    }

    @Test
    void downloadUrl() {
        assertEquals("https://registry.npmjs.org/npm/-/npm-10.9.0.tgz", new NpmTool().downloadUrl("10.9.0"));
    }

    private static Path fakePackage(String version, String... binFiles) throws IOException {
        Path home = DefaultConstants.getVersionedToolFolder("npm", version);
        Path bin = Files.createDirectories(home.resolve("bin"));

        for (String file : binFiles) {
            Files.createFile(
                    bin.resolve(file),
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
        }

        return home;
    }
}
