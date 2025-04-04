package org.jboss.pnc.jshim.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.jboss.pnc.jshim.backend.tools.BasicTool;
import org.jboss.pnc.jshim.backend.tools.MavenTool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MavenToolTest {

    private static Path toolFolder = Paths.get("src/test/resources/tools");
    MavenTool mavenTool = new MavenTool();

    @BeforeAll
    static void before() {
        DefaultConstants.setOverrideDataPath(toolFolder);
    }

    @AfterAll
    static void after() {
        DefaultConstants.resetOverrideDataPath();
    }

    @Test
    void testVersion() {
        Map<String, Path> versions = mavenTool.availableVersions();

        assertTrue(versions.containsKey("3.1.0"));
        assertTrue(versions.containsKey("3.2.0"));
    }

    @Test
    void testShims() {
        Map<String, Path> shims = BasicTool.shimAndSymlink(mavenTool, "3.1.0");

        assertTrue(shims.containsKey("mvn"));
        assertTrue(shims.get("mvn").toString().contains("tools/downloaded/maven/maven-3.1.0/bin/mvn"));
        assertFalse(shims.containsKey("not-binary"));
        assertFalse(shims.containsKey("folder"));
    }

    @Test
    void raiseErrorIfVersionNotFound() {
        assertThrows(RuntimeException.class, () -> BasicTool.shimAndSymlink(mavenTool, "9.9.9"));
    }
}