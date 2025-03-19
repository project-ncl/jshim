package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

class MavenMetadataTest {
    private static Path localMavenMetadata = Paths.get("src/test/resources/maven-metadata/maven-metadata.xml");
    private static final String VERSIONS_URL = "https://repo1.maven.org/maven2/org/apache/maven/apache-maven/maven-metadata.xml";

    @Test
    void testGetLocalVersions() throws Exception {

        List<String> versions = MavenMetadata.getVersions(localMavenMetadata.toFile());

        assertTrue(versions.contains("2.0.9"));
        assertTrue(versions.contains("2.0.10"));
    }

    @Test
    void testGetUrlVersions() throws Exception {
        List<String> versions = MavenMetadata.getVersions(VERSIONS_URL);
        assertTrue(versions.contains("2.0.9"));
        assertTrue(versions.contains("2.0.10"));
    }

}