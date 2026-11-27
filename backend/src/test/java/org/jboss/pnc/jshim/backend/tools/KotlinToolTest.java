package org.jboss.pnc.jshim.backend.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class KotlinToolTest {

    @Test
    void downloadUrl() {
        assertEquals(
                "https://github.com/JetBrains/kotlin/releases/download/v2.1.0/kotlin-compiler-2.1.0.zip",
                new KotlinTool().downloadUrl("2.1.0"));
    }

    @Test
    void onlyStableVersionsAreKept() {
        List<String> versions = List.of(
                "0.14.451",
                "1.0.0-beta-1038",
                "1.9.20",
                "2.1.0-Beta1",
                "2.1.0-RC",
                "2.1.0",
                "2.1.20-dev-123");

        assertEquals(List.of("1.9.20", "2.1.0"), KotlinTool.filterStableVersions(versions));
    }
}
